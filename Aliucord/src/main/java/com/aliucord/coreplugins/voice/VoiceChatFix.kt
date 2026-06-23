package com.aliucord.coreplugins.voice

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import co.discord.media_engine.VideoDecoder
import co.discord.media_engine.VideoInputDeviceDescription
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.coreplugins.voice.VoiceChatFixPayload.DaveInvalidCommitWelcome
import com.aliucord.coreplugins.voice.VoiceChatFixPayload.DaveTransitionReady
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.updater.ManagerBuild
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.GsonUtils.fromJson
import com.aliucord.utils.ReflectUtils
import com.aliucord.utils.SemVer
import com.aliucord.utils.ViewUtils.addTo
import com.discord.play_delivery.PlayAssetDeliveryNativeWrapper
import com.discord.rtcconnection.mediaengine.MediaEngineConnection
import com.discord.rtcconnection.socket.io.Opcodes
import com.discord.rtcconnection.socket.io.Payloads
import com.discord.rtcconnection.socket.io.Payloads.Protocol.ProtocolInfo
import com.discord.stores.StoreMediaEngine
import com.discord.stores.StoreMediaSettings
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.debug.DebugPrintBuilder
import com.discord.widgets.settings.WidgetSettingsVoice
import com.discord.widgets.voice.controls.VoiceControlsSheetView
import com.discord.widgets.voice.sheet.WidgetVoiceSettingsBottomSheet
import com.lytefast.flexinput.R
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.io.File
import java.util.Collections
import java.util.WeakHashMap
import kotlin.math.pow
import b.a.q.m0.c.e as MediaEngineConnectionLegacy
import b.a.q.m0.c.`e$h` as MediaEngineConnectionLegacy_SetCodecs
import b.a.q.n0.a as RtcControlSocket
import b.a.q.n0.`a$j` as RtcControlSocket_OnMessage
import b.a.q.n0.`a$k` as RtcControlSocket_Connect

data class SecureFrames(
    val epochAuthenticator: String,
    val version: Int,
)

internal class VoiceChatFix : CorePlugin(Manifest("VoiceChatFix"))  {
    override val isHidden = false
    override val isRequired = true
    private val debugInfo = LinkedHashMap<String, String>()
    private var currentSocket: RtcControlSocket? = null
    private val epochPreparedSockets = Collections.newSetFromMap(WeakHashMap<RtcControlSocket, Boolean>())
    private val pendingProposals = WeakHashMap<RtcControlSocket, MutableList<ByteString>>()
    private var prevSocket: RtcControlSocket? = null

    private val libVersion = runCatching {
        Class.forName("com.aliucord.voice.BuildConfig")
            .getField("VERSION")
            .get(null) as String
    }.getOrNull()

    @Volatile
    private var supportedModes: List<String>? = null

    private companion object {
        // Native libs and webrtc dex are built together (aliuvoice aar)
        // the lib version must match exactly but injector & patches only need a min version
        const val EXPECTED_LIB_VERSION = com.aliucord.voice.BuildConfig.VERSION
        val MIN_INJECTOR: SemVer = SemVer(2, 3, 2)
        val MIN_PATCHES: SemVer = SemVer(1, 5, 0)
    }


    // This is true if lib version matches and injector & patches >= min version
    private val isFullySupported: Boolean
        get() {
            val injector = ManagerBuild.metadata?.injectorVersion ?: return false
            val patches = ManagerBuild.metadata?.patchesVersion ?: return false
            return libVersion == EXPECTED_LIB_VERSION &&
                injector >= MIN_INJECTOR &&
                patches >= MIN_PATCHES
        }

    init {
        manifest.version = libVersion ?: "0.0.0"
        manifest.description = if (isFullySupported) {
            "Implementation of DAVE, which supports E2EE voice, camera and screenshare support to Aliucord (v$libVersion)"
        } else {
            "(non-functional, requires base app update!) Implementation of DAVE, which supports E2EE voice, camera and screenshare (lib ${libVersion?.let { "v$it" } ?: "missing"}, need $EXPECTED_LIB_VERSION)"
        }
        manifest.authors = arrayOf(
            Manifest.Author("cilly", 368398754077868032L),
            Manifest.Author("secp192k1", 477497542205243392L),
        )
        settingsTab = SettingsTab(VoiceChatFixSettings.Sheet::class.java, SettingsTab.Type.BOTTOM_SHEET)
    }

    private fun chooseTransportMode(): String {
        val pref = VoiceChatFixSettings.transportEncryption
        val modes = supportedModes ?: return pref
        return when {
            pref in modes -> pref
            else -> modes.firstOrNull { it.startsWith("aead_") } ?: pref
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    override fun start(context: Context) {
        // Force usage of updated transport encryption
        patcher.before<ProtocolInfo>(
            String::class.java,
            Int::class.javaPrimitiveType!!,
            String::class.java,
        ) { (param, _: String, _: Int, mode: String) ->
            if (mode == "xsalsa20_poly1305") {
                param.args[2] = if (libVersion != null) {
                    VoiceChatFixSettings.transportEncryption
                } else {
                    "xsalsa20_poly1305_lite_rtpsize"
                }
            }
            setDebug("Transport", param.args[2] as String)
        }

        val injector = ManagerBuild.metadata?.injectorVersion
        val patches = ManagerBuild.metadata?.patchesVersion
        logger.info("Core: ${com.aliucord.BuildConfig.VERSION}")
        logger.info("Injector: $injector")
        logger.info("Patches: $patches")
        logger.info("VoiceChatFix: $libVersion")
        if (!isFullySupported) {
            logger.warn(
                "Mismatched versions, will only patch transport encryption protocol " +
                    "(lib=$libVersion need=$EXPECTED_LIB_VERSION, injector=$injector need>=$MIN_INJECTOR, patches=$patches need>=$MIN_PATCHES)"
            )
            return
        }

        // Stock discord jar shadows org.webrtc on the compile classpath, so the
        // updated class with this field can't be referenced directly
        runCatching {
            Class.forName("org.webrtc.HardwareVideoEncoder")
                .getField("MAX_ENCODER_Q_SIZE")
                .setInt(null, VoiceChatFixSettings.encoderQueueSize)
        }.onFailure { logger.error("Failed to set encoder queue size", it) }

        patchPrivacyCodeView()
        patchUserSheetVerificationCode()

        // Handle new binary voice gateway events
        // WebSocketListener is RtcControlSocket's superclass; the child class doesn't have
        // an override for this method so we have to patch its superclass and figure out if
        // it is from RtcControlSocket
        patcher.before<WebSocketListener>(
            "onMessage",
            WebSocket::class.java,
            ByteString::class.java,
        ) { (param, websocket: WebSocket, bytes: ByteString) ->
            if (this !is RtcControlSocket) return@before
            param.result = Unit.a

            if (this.s != websocket) return@before
            handleBinaryMessage(this, bytes)
        }

        // During payload creation, if it is an Identify payload, replace it with a new one
        // that contains the dave protocol version
        patcher.before<Payloads.Outgoing>(
            Int::class.javaPrimitiveType!!,
            Any::class.java
        ) { (param, opcode: Int, data: Any) ->
            if (opcode == Opcodes.IDENTIFY) {
                val d = data as Payloads.Identify
                // Request & override a higher framerate from the server
                runCatching {
                    d.streams.forEach { stream ->
                        ReflectUtils.setField(stream, "maxFrameRate", VoiceChatFixSettings.videoFramerate)
                    }
                }.onFailure { logger.error("Failed to set stream maxFrameRate", it) }
                param.args[1] = NewIdentifyPayload(
                    serverId = d.serverId,
                    userId = d.userId.toString(),
                    sessionId = d.sessionId,
                    token = d.token,
                    // Keep the original stream declaration so the server advertises the
                    // broadcaster's video source to viewers (or else dropped as "not routed").
                    video = d.video,
                    streams = d.streams,
                    // 0 disables DAVE (transport-only); toggle for viewer-compat A/B testing.
                    maxDaveProtocolVersion = if (VoiceChatFixSettings.daveEnabled) 1 else 0
                )
                logger.debug("Replacing Identify payload")
                logger.debug("Before: $d")
                logger.debug("After: ${param.args[1]}")
            }

            // Patch protocol payload to include encode/decode fields
            if (opcode == Opcodes.SELECT_PROTOCOL) {
                val d = data as Payloads.Protocol
                val base = NewSelectProtocolPayload.from(d)
                val mode = chooseTransportMode()
                param.args[1] = base.copy(
                    data = Payloads.Protocol.ProtocolInfo(base.data.address, base.data.port, mode)
                )
                logger.debug("Replacing Protocol payload (transport=$mode)")
                logger.debug("Before: $d")
                logger.debug("After: ${param.args[1]}")
            }
        }

        // Patches codec setter to pass all available codecs to native lib
        patcher.before<MediaEngineConnectionLegacy>(
            "z", // runSynchronized
            Function1::class.java,
        ) { (param, callback: Function1<*, *>) ->
            if (callback is MediaEngineConnectionLegacy_SetCodecs) {
                // org.webrtc.H264Utils.getDefaultH264Params()
                val params = mapOf(
                    "level-asymmetry-allowed" to "1",
                    "packetization-mode" to "1",
                    "profile-level-id" to "42e01f",
                )
                j.setCodecs(
                    callback.`$audioEncoder`,
                    callback.`$videoEncoder`,
                    arrayOf(callback.`$audioDecoder`),
                    this.i.filter { it.c == "video" }.map { codec ->
                        VideoDecoder(codec.a, codec.d, codec.e, params)
                    }.toTypedArray()
                )
                this.i.filter { it.c == "audio" }.map { it.a }.distinct().takeIf { it.isNotEmpty() }
                    ?.let { setDebug("Audio Codec", it.joinToString(", ")) }
                this.i.filter { it.c == "video" }.map { it.a }.distinct().takeIf { it.isNotEmpty() }
                    ?.let { setDebug("Video Codec", it.joinToString(", ")) }
                param.result = null
            }
        }

        patcher.before<RtcControlSocket_OnMessage>(
            RtcControlSocket::class.java,
            WebSocket::class.java,
            Payloads.Incoming::class.java,
        ) { param ->
            val message = param.args[2] as Payloads.Incoming
            when (message.opcode) {
                Opcodes.READY -> runCatching {
                    val modes = org.json.JSONObject(message.data.toString()).optJSONArray("modes")
                    supportedModes = modes?.let { arr -> List(arr.length()) { arr.getString(it) } }
                }.onFailure { logger.error("Failed to read READY transport modes", it) }
                Opcodes.MEDIA_SINK_WANTS ->
                    // pixelCounts is only mentioned in streams/screenshare if
                    // RtcConnection has enableMediaSinkWants == false
                    param.args[2] = Payloads.Incoming(message.opcode, message.data.d().apply {
                        // Remove pixelCounts since it messes up the default handler's conversion from
                        // JsonObject to Map<String, Number>
                        @SuppressLint("CheckResult")
                        a.remove("pixelCounts")
                    })
                else -> logger.warn("Unhandled Opcode: ${message.opcode}")
            }
        }

        patcher.before<RtcControlSocket_OnMessage>("invoke") { param ->
            if (this.`$message`.opcode == Opcodes.MEDIA_SINK_WANTS) {
                param.result = Unit.a
            }
        }

        // Never allow resuming; this is because DAVE state may be inconsistent upon resuming.
        // Normally, newer clients handle this with buffered resume - which replays DAVE messages -
        // but DiscordKt does not use this new feature as it's on an older voice gateway version.
        // TODO maybe: implement buffered resume properly, since this may be disruptive when for example
        // roaming on a mobile connection (fresh connections are slightly slower)
        patcher.before<RtcControlSocket_Connect>("invoke") {
            `this$0`.C = false // RtcControlSocket.resumable = false
        }

        // Handle new (json) voice gateway events
        patcher.after<RtcControlSocket_OnMessage>("invoke") {
            val socket: RtcControlSocket = this.`this$0`

            // Not sure what this check is for but it's done in original code
            if (socket.s != this.`$webSocket`) {
                return@after
            }

            if (socket != currentSocket) {
                prevSocket = currentSocket
                currentSocket = socket
            }
            refreshConnInfo()

            val message = this.`$message`
            val gson = socket.n

            if (message.opcode == Opcodes.SELECT_PROTOCOL_ACK) {
                // message.data.jsonObject.entries["secure_frames_version"]?.jsonPrimitive?.asInt()
                val ver = message.data.d().a["secure_frames_version"]?.e()?.c() ?: 0
                logger.debug("Protover: $ver")
                handleOnProtocolSelectAck(socket, ver)
            }

            // We need to call the encoder so screenshares actually display something
            // Also apply user video settings on every sink-wants update
            if (message.opcode == Opcodes.MEDIA_SINK_WANTS) {
                applyVideoSettings(socket)
            }

            val payload = VoiceChatFixPayload.deserialize(gson, message)
                ?: return@after
            logger.debug("VoiceChatFix payload ${Opcodes.friendly(message.opcode)}: $payload")

            socket.connections.forEach { connection ->
                when (payload) {
                    is VoiceChatFixPayload.ClientsConnect -> {
                        // TODO: native can handle a list of users
                        logger.debug("Connect: ${payload.userIds}")
                        connection.connectUsers(payload.userIds)
                    }
                    is VoiceChatFixPayload.ClientDisconnect -> {
                        logger.debug("Disconnect: ${payload.userId}")
                        connection.destroyUser(payload.userId)
                    }
                    is VoiceChatFixPayload.DavePrepareTransition -> {
                        connection.prepareSecureFramesTransition(
                            transitionId = payload.transitionId,
                            protocolVersion = payload.protocolVersion
                        ) {
                            socket.send(DaveTransitionReady(payload.transitionId))
                        }
                    }
                    is VoiceChatFixPayload.DaveExecuteTransition -> {
                        connection.executeSecureFramesTransition(payload.transitionId)
                    }
                    is VoiceChatFixPayload.DavePrepareEpoch -> {
                        // MLS group id is the rtcServerId-derived groupId, NOT the channelId.
                        // Using channelId reinitialises the MLS session with the wrong group, so the
                        // server's commit/welcome no longer match ("Unexpected group ID in MLS welcome"),
                        // the join fails (joined:false -> DAVE_MLS_INVALID_COMMIT_WELCOME) and the
                        // encryptor falls back to a dead key ratchet, leaving viewers unable to decrypt.
                        val groupId = socket.rtcConnection?.groupId
                        logger.debug("Preparing secure frames epoch (request) for $groupId")
                        connection.prepareSecureFramesEpoch(
                            epoch = payload.epoch.toString(),
                            transitionId = payload.epoch,
                            groupId = groupId?.toString() ?: ""
                        )
                        connection.getMLSKeyPackageB64 { keyPackageB64 ->
                            val bytes = keyPackageB64.decodeBase64ToArray()!!
                            logger.debug("Received MLS Key package, sending over")
                            socket.send(Opcodes.DAVE_MLS_KEY_PACKAGE, ByteString(bytes))
                        }
                    }
                }
            }
        }

        // Use guid-based device selection rather than index-based
        patcher.before<StoreMediaEngine>(
            "handleVideoInputDevices",
            Array<VideoInputDeviceDescription>::class.java,
            String::class.java,
            Function1::class.java
        ) { (_, _: Any, guid: String?) ->
            val device = guid ?: "default"
            logger.debug("Setting video input device $device")
            // Push capture settings first so the camera opens at the target framerate directly,
            // instead of the native default 30fps followed by an immediate session re-open.
            currentSocket?.let(::applyVideoSettings)
            mediaEngine.i().setVideoInputDevice(device)
        }

        patchKrisp()
    }

    // Upon protocol selection ack, start preparing for dave
    private fun handleOnProtocolSelectAck(socket: RtcControlSocket, version: Int) {
        currentSocket = socket
        socket.rtcConnection?.rtcServerId?.let { setDebug("Server", it) }
        setDebug("E2EE", if (version >= 1) "DAVE v$version" else "Off (transport only)")

        val groupId = socket.rtcConnection?.groupId
            ?: return logger.error("No rtc connection upon protocol select ack", null)
        socket.connections.forEach { connection ->
            if (version == 0) {
                logger.debug("No secure frames, bye!")
                // TODO: Are these values correct?
                connection.prepareSecureFramesTransition(0, 0) {
                    logger.debug("Transitioned to secure frame ver 0")
                }
                return@forEach
            }
            socket.rtcConnection?.run {
                logger.debug("conn - ch ${this.channelId} sr ${this.rtcServerId} gr $groupId sk $d0")
                StringBuilder().let {
                    debugPrint(DebugPrintBuilder(it))
                    logger.debug("debg - $it")
                }
            }
            logger.debug("Preparing secure frames epoch for $groupId")
            // TODO: Are these values correct?
            connection.prepareSecureFramesEpoch("1", 1, groupId.toString())
            logger.debug("Grabbing MLS Key..")
            connection.getMLSKeyPackageB64 { keyPackageB64 ->
                val bytes = keyPackageB64.decodeBase64ToArray()!!
                logger.debug("Received MLS Key package, sending over")
                socket.send(Opcodes.DAVE_MLS_KEY_PACKAGE, ByteString(bytes))
            }
        }
        if (version >= 1) {
            val queued = synchronized(pendingProposals) {
                epochPreparedSockets.add(socket)
                pendingProposals.remove(socket)
            }
            queued?.forEach { handleBinaryMessage(socket, it) }
        }
    }

    private fun handleBinaryMessage(socket: RtcControlSocket, bytestr: ByteString) {
        logger.debug("Received binary message ${bytestr.encodeBase64()}")
        val reader = ByteReader(bytestr)
        // First byte is the opcode, this is contrary to most docs because we are using an older version
        // of the voice gateway without resuming support.
        // On newer versions, the first two bytes denote the sequence, and the third one is the opcode.
        // The sequence number is not present on old voice gateway versions.
        val opcode = reader.readUint8()

        when (opcode) {
            Opcodes.DAVE_MLS_EXTERNAL_SENDER -> {
                val encoded = reader.collectAsByteString().encodeBase64()
                logger.debug("MLSExternalSender: $encoded")
                socket.connections.forEach { connection ->
                    connection.updateMLSExternalSenderB64(encoded)
                }
            }
            Opcodes.DAVE_MLS_PROPOSALS -> {
                // Binary frames skip queue, proposals beat SELECT_PROTOCOL_ACK.
                synchronized(pendingProposals) {
                    if (socket !in epochPreparedSockets) {
                        logger.debug("Epoch not prepared yet, queueing MLS proposals")
                        pendingProposals.getOrPut(socket) { mutableListOf() }.add(bytestr)
                        return
                    }
                }
                val encoded = reader.collectAsByteString().encodeBase64()
                logger.debug("MLSProposals: $encoded")
                socket.connections.forEach { connection ->
                    connection.processMLSProposalsB64(encoded) { commitWelcome ->
                        logger.debug("MLSProposals commit received, sending over..: $commitWelcome")
                        socket.send(Opcodes.DAVE_MLS_COMMIT_WELCOME, ByteString(commitWelcome.decodeBase64ToArray()))
                    }
                }
            }
            Opcodes.DAVE_MLS_ANNOUNCE_COMMIT_TRANSITION -> {
                val transitionId = reader.readUint16()
                val encoded = reader.collectAsByteString().encodeBase64()
                logger.debug("MLSAnnounceCommitTransition $transitionId: $encoded") //encodeBase64
                socket.connections.forEach { connection ->
                    connection.prepareMLSCommitTransitionB64(
                        transitionId = transitionId,
                        commit = encoded,
                    ) { processedCommit, protocolVersion, rosterChange ->
                        logger.debug("MLSAnnounceCommitTransition processed: $processedCommit, ver: $protocolVersion, changes: $rosterChange")
                        if (!processedCommit) {
                            socket.send(DaveInvalidCommitWelcome(transitionId = transitionId))
                        }
                    }
                }
            }
            Opcodes.DAVE_MLS_WELCOME -> {
                val transitionId = reader.readUint16()
                val encoded = reader.collectAsByteString().encodeBase64()
                logger.debug("MLSWelcome $transitionId: $encoded")
                socket.connections.forEach { connection ->
                    connection.processMLSWelcomeB64(
                        transitionId = transitionId,
                        welcome = encoded,
                    ) { joinedGroup, protocolVersion, rosterChange ->
                        logger.debug("MLSWelcome Processed, joined: $joinedGroup, ver: $protocolVersion, changes: $rosterChange")
                        if (!joinedGroup) {
                            socket.send(DaveInvalidCommitWelcome(transitionId = transitionId))
                        }
                    }
                }
            }
        }
    }

    val encryptionViewId = View.generateViewId()
    private val connInfoViewId = View.generateViewId()
    private val verificationRowId = View.generateViewId()
    private var sheetUserId = 0L
    var newestCode = ""
    var onCodeUpdate: (String) -> Unit = {}
    private var connInfoText = "Not connected"
    var onConnInfoUpdate: (String) -> Unit = {}

    private fun applyVideoSettings(socket: RtcControlSocket) {
        socket.connections.forEach { connection ->
            runCatching {
                connection.setEncodingQuality(
                    150_000,
                    VoiceChatFixSettings.videoBitrateKbps * 1000,
                    VoiceChatFixSettings.videoWidth,
                    VoiceChatFixSettings.videoHeight,
                    VoiceChatFixSettings.videoFramerate,
                )
            }.onFailure { logger.error("Failed to apply video encode settings", it) }
        }
        setDebug("Bitrate", "${VoiceChatFixSettings.videoBitrateKbps} kbps")
        setDebug("Resolution", "${VoiceChatFixSettings.videoWidth} x ${VoiceChatFixSettings.videoHeight}")
        setDebug("FPS", VoiceChatFixSettings.videoFramerate.toString())
    }

    private fun setDebug(key: String, value: String) {
        debugInfo[key] = value
        refreshConnInfo()
    }

    private fun renderDebugInfo(): String =
        debugInfo.entries.joinToString("\n") { "${it.key}:  ${it.value}" }

    private fun refreshConnInfo() {
        val sb = StringBuilder()
        val head = renderDebugInfo()
        if (head.isNotEmpty()) sb.append(head).append("\n\n")
        // Fallback to the previous socket if the current one is inactive to keep the
        // overlay populated instead of going blank.
        val rtc = currentSocket?.rtcConnections?.firstOrNull()
            ?: prevSocket?.rtcConnections?.firstOrNull()
        if (rtc != null) {
            runCatching {
                val b = StringBuilder()
                rtc.debugPrint(DebugPrintBuilder(b))
                sb.append(b.toString().trim())
            }.onFailure { sb.append("(debugPrint failed: ${it.message})") }
        } else if (head.isEmpty()) {
            sb.append("Not connected")
        }
        connInfoText = sb.toString().ifEmpty { "Not connected" }
        onConnInfoUpdate(connInfoText)
    }

    private fun codeBlock(ctx: Context): TextView = TextView(ctx).apply {
        typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.sourcecodepro_semibold)
        setTextColor(Color.WHITE)
        textSize = 11f
        gravity = Gravity.START
        isSingleLine = false
        maxLines = 400
        setLineSpacing(2.dp.toFloat(), 1f)
        background = GradientDrawable().apply {
            cornerRadius = 8.dp.toFloat()
        }
        setPadding(12.dp, 10.dp, 12.dp, 10.dp)
    }

    private fun cardTitle(ctx: Context, text: String): TextView =
        TextView(ctx, null, 0, R.i.UiKit_ListItem_Icon).apply {
            this.text = text
            setTextColor(Color.WHITE)
        }

    private fun collapsibleTitle(ctx: Context, label: String, body: View, expanded: Boolean = false): TextView {
        body.visibility = if (expanded) View.VISIBLE else View.GONE
        // Use an en-space after the chevron; a plain space renders too tight against the label.
        fun titleText(open: Boolean) = (if (open) "▾ " else "▸ ") + label
        return cardTitle(ctx, titleText(expanded)).apply {
            setOnClickListener {
                val show = body.visibility != View.VISIBLE
                body.visibility = if (show) View.VISIBLE else View.GONE
                text = titleText(show)
            }
        }
    }

    private fun newCard(ctx: Context, cardId: Int): CardView = CardView(ctx).apply {
        setCardBackgroundColor(ColorCompat.getColor(this, R.c.white_alpha_24))
        radius = 8.dp.toFloat()
        elevation = 0f
        id = cardId
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            bottomMargin = 16.dp
        }
    }

    // Adds encryption info and voice privacy code to the voice bottom sheet
    private fun patchPrivacyCodeView() {
        // Patches on new connection to add our callback when the epoch authenticator changes
        patcher.after<StoreMediaEngine>(
            "handleNewConnection",
            MediaEngineConnection::class.java,
        ) { (_, conn: MediaEngineConnectionLegacy) ->
            if (conn.type != MediaEngineConnection.Type.DEFAULT) return@after
            logger.debug("setting secure frames callback...")
            newestCode = ""
            onCodeUpdate("")
            debugInfo.clear()
            setDebug("Status", "Connected")
            conn.j.setSecureFramesStateUpdateCallback { epochStr ->
                val frames = GsonUtils.gson.fromJson(epochStr, SecureFrames::class.java)
                logger.info("cb $epochStr -> $frames")
                newestCode = if (frames.version < 1) "" else formatFingerprint(frames.epochAuthenticator)
                onCodeUpdate(newestCode)
            }
        }

        // Patches view to add our encryption info if not already added
        patcher.patch(
            // every part of this signature is hell
            VoiceControlsSheetView::class.java.declaredMethods.find { it.name == "configureUI-3jxq49Y" }!!
        ) { param ->
            val self = param.thisObject as VoiceControlsSheetView
            if (self.findViewById<CardView?>(encryptionViewId) != null) return@patch
            val ctx = self.context

            newCard(ctx, encryptionViewId).addTo(self, 1) card@{
                LinearLayout(ctx).addTo(this) {
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    orientation = LinearLayout.VERTICAL

                    val t1 = TextView(ctx, null, 0, R.i.UiKit_ListItem_Icon).addTo(this)
                    val t2 = codeBlock(ctx).addTo(this) {
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                            topMargin = 8.dp
                            // Inset from the card's bottom edge. Lives on the code block (not the
                            // container) so it collapses when the block is GONE (not encrypted).
                            bottomMargin = 12.dp
                        }
                        gravity = Gravity.CENTER
                        textSize = 16f
                        setBackgroundColor(Color.TRANSPARENT)
                    }

                    onCodeUpdate = { code ->
                        Utils.mainThread.post {
                            if (code.isNotEmpty()) {
                                t1.setCompoundDrawablesWithIntrinsicBounds(
                                    ResourcesCompat.getDrawable(ctx.resources, R.e.ic_small_lock_green_24dp, null),
                                    null, null, null
                                )
                                t1.text = "End-to-end encrypted"
                                t2.text = code.replaceRange(17, 18, "\n").replace(" ", "     ")
                                t2.visibility = View.VISIBLE
                                this@card.setOnClickListener {
                                    Utils.setClipboard("Voice privacy code", code)
                                    Utils.showToast("Copied to clipboard")
                                }
                            } else {
                                t1.setCompoundDrawablesWithIntrinsicBounds(
                                    ResourcesCompat.getDrawable(ctx.resources, R.e.ic_x_red_24dp, null),
                                    null, null, null
                                )
                                t1.text = "Not end-to-end encrypted"
                                t2.visibility = View.GONE
                                this@card.setOnClickListener(null)
                            }
                        }
                    }
                    onCodeUpdate(newestCode)
                }
            }

            if (!VoiceChatFixSettings.showConnInfo) return@patch
            newCard(ctx, connInfoViewId).addTo(self, 2) {
                LinearLayout(ctx).addTo(this) {
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    orientation = LinearLayout.VERTICAL

                    val info = codeBlock(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    }
                    collapsibleTitle(ctx, " Connection Info", info).addTo(this)
                    info.addTo(this)
                    onConnInfoUpdate = { text ->
                        Utils.mainThread.post { info.text = text }
                    }
                    info.setOnClickListener {
                        Utils.setClipboard("VoiceChatFix connection info", connInfoText)
                        Utils.showToast("Copied to clipboard")
                    }
                    refreshConnInfo()
                }
            }
        }
    }

    private fun formatFingerprint(b64: String): String {
        if (b64.isEmpty()) return ""
        val data = b64.decodeBase64ToArray() ?: return ""
        val groupSize = 5
        val desiredLen = 30
        if (data.size < desiredLen) return ""
        val groupModulus = 10.0.pow(groupSize).toULong()
        var result = ""
        var i = 0
        while (i < desiredLen) {
            var groupValue = 0UL
            var j = groupSize
            while (j >= 1) {
                val n = data[i + groupSize - j].toUByte().toULong()
                groupValue = (groupValue shl 8) or n
                j -= 1
            }
            result += " " + (groupValue % groupModulus).toString().padStart(groupSize, '0')
            i += groupSize
        }
        return result.trimStart()
    }

    private fun getPairwiseCode(userId: String, callback: (String?) -> Unit) {
        val connection = currentSocket?.connections?.firstOrNull()
        if (connection == null) {
            callback(null)
            return
        }
        runCatching {
            connection.getMLSPairwiseFingerprintB64(1, userId) { fp ->
                Utils.mainThread.post { callback(formatFingerprint(fp).ifEmpty { null }) }
            }
        }.onFailure {
            logger.error("Failed to get pairwise fingerprint for $userId", it)
            Utils.mainThread.post { callback(null) }
        }
    }

    private fun addVerificationRow(root: LinearLayout, userId: Long) {
        if (userId == 0L) return
        val container = (root.getChildAt(0) as? LinearLayout) ?: root
        val ctx = container.context
        val codeView: TextView
        val existing = root.findViewById<View?>(verificationRowId)
        if (existing == null) {
            val ripple = TypedValue()
            //ctx.theme.resolveAttribute(android.R.attr.selectableItemBackground, ripple, true)
            val title = TextView(ctx).apply {
                text = "Verification Code"
                setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorHeaderPrimary))
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_semibold)
                textSize = 16f
            }
            codeView = TextView(ctx).apply {
                setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorTextMuted))
                textSize = 14f
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.sourcecodepro_semibold)
                setPadding(0, 2.dp, 0, 0)
            }
            LinearLayout(ctx).apply {
                id = verificationRowId
                orientation = LinearLayout.VERTICAL
                isClickable = true
                isFocusable = true
                setBackgroundResource(ripple.resourceId)
                setPadding(16.dp, 10.dp, 16.dp, 10.dp)
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                addView(title)
                addView(codeView)
                setOnClickListener {
                    val c = codeView.text?.toString().orEmpty()
                    if (c.isNotEmpty() && c != "Unavailable" && c != "…") {
                        Utils.setClipboard("Verification code", c)
                        Utils.showToast("Copied to clipboard")
                    }
                }
                container.addView(this)
            }
        } else {
            codeView = (existing as LinearLayout).getChildAt(1) as TextView
        }
        codeView.text = "…"
        getPairwiseCode(userId.toString()) { c -> codeView.text = c ?: "Unavailable" }
    }

    private fun patchUserSheetVerificationCode() {
        val sheetClass = runCatching { Class.forName("com.discord.widgets.user.usersheet.WidgetUserSheet") }.getOrNull() ?: return
        val viewClass = runCatching { Class.forName("com.discord.widgets.user.usersheet.UserProfileVoiceSettingsView") }.getOrNull() ?: return

        sheetClass.declaredMethods.firstOrNull { it.name == "configureVoiceSection" }?.let { m ->
            patcher.patch(m, PreHook { param ->
                sheetUserId = runCatching {
                    val vs = param.args[0] ?: return@runCatching 0L
                    val user = vs.javaClass.getMethod("getUser").invoke(vs) ?: return@runCatching 0L
                    user.javaClass.getMethod("getId").invoke(user) as Long
                }.getOrDefault(0L)
            })
        }

        viewClass.declaredMethods.firstOrNull { it.name == "updateView" }?.let { m ->
            patcher.patch(m, Hook { param ->
                runCatching { addVerificationRow(param.thisObject as LinearLayout, sheetUserId) }
                    .onFailure { logger.error("Failed to add verification row", it) }
            })
        }
    }

    // TODO: Krisp
    // Disable krisp-related settings as they are broken; Krisp is fetched from play delivery assets
    // which is likely a huge pain to use properly on a patched app
    private fun patchKrisp() {
        patcher.after<WidgetSettingsVoice>(
            "configureUI",
            WidgetSettingsVoice.Model::class.java,
        ) {
            val binding = WidgetSettingsVoice.`access$getBinding$p`(this)

            val krispToggle = binding.k
            krispToggle.l.b().isClickable = false
            krispToggle.alpha = 0.3f

            val krispVadToggle = binding.h
            krispVadToggle.l.b().isClickable = false
            krispVadToggle.alpha = 0.3f
        }
        patcher.after<WidgetVoiceSettingsBottomSheet>(
            "configureUI",
            WidgetVoiceSettingsBottomSheet.ViewState::class.java,
        ) {
            val binding = WidgetVoiceSettingsBottomSheet.`access$getBinding$p`(this)
            val krispToggle = binding.d
            krispToggle.visibility = View.GONE
        }
        patcher.before<StoreMediaSettings.VoiceConfiguration>(
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            StoreMediaSettings.VadUseKrisp::class.java,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            StoreMediaSettings.NoiseProcessing::class.java,
            Float::class.javaPrimitiveType!!,
            MediaEngineConnection.InputMode::class.java,
            Float::class.javaPrimitiveType!!,
            Map::class.java,
            Map::class.java,
            Map::class.java,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
        ) { param ->
            param.args[3] = StoreMediaSettings.VadUseKrisp.Disabled
            if (param.args[6] != StoreMediaSettings.NoiseProcessing.None) {
                param.args[6] = StoreMediaSettings.NoiseProcessing.Suppression
            }
        }
    }

    // TODO: Krisp
    @Suppress("unused")
    private fun setupKrisp(context: Context) {
        runCatching {
            val srcModel = File(Constants.BASE_PATH, "krisp/thz")
            if (!File(srcModel, "VAD_model.kw").exists()) {
                logger.warn("Krisp models not found at ${srcModel.absolutePath}, Krisp disabled")
                return
            }
            val dest = File(context.filesDir, "krisp")
            val destModel = File(dest, "thz").apply { mkdirs() }
            srcModel.listFiles()?.forEach { src ->
                val out = File(destModel, src.name)
                if (!out.exists() || out.length() != src.length()) src.copyTo(out, overwrite = true)
            }
            PlayAssetDeliveryNativeWrapper.setKrispAssetPackLocation(dest.absolutePath)
            logger.info("Krisp models ready at ${dest.absolutePath}")
        }.onFailure { logger.error("Failed to set up Krisp models", it) }
    }
}

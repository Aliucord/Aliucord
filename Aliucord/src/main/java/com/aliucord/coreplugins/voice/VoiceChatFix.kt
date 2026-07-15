package com.aliucord.coreplugins.voice

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import co.discord.media_engine.Connection
import co.discord.media_engine.VideoDecoder
import co.discord.media_engine.VideoInputDeviceDescription
import com.aliucord.Constants
import com.aliucord.Utils
import com.aliucord.api.GatewayAPI
import com.aliucord.coreplugins.voice.VoiceChatFixPayload.DaveInvalidCommitWelcome
import com.aliucord.coreplugins.voice.VoiceChatFixPayload.DaveTransitionReady
import com.aliucord.coreplugins.voice.VoiceChatTimers.backstopTimeSelected
import com.aliucord.coreplugins.voice.VoiceChatTimers.callStartTimes
import com.aliucord.coreplugins.voice.VoiceChatTimers.callTimersLines
import com.aliucord.coreplugins.voice.VoiceChatTimers.trackCallStart
import com.aliucord.coreplugins.voice.VoiceChatTimers.requestChannelInfo
import com.aliucord.coreplugins.voice.model.ChannelInfo
import com.aliucord.coreplugins.voice.model.NewIdentifyPayload
import com.aliucord.coreplugins.voice.model.NewSelectProtocolPayload
import com.aliucord.coreplugins.voice.model.SecureFrames
import com.aliucord.coreplugins.voice.model.TransportModes
import com.aliucord.coreplugins.voice.model.VoiceChannelStartTime
import com.aliucord.coreplugins.voice.model.VoiceCloseCodes
import com.aliucord.coreplugins.voice.ui.addDisableVideoRow
import com.aliucord.coreplugins.voice.ui.addMuteSoundboardRow
import com.aliucord.coreplugins.voice.ui.addVerificationRow
import com.aliucord.coreplugins.voice.ui.isVideoDisabled
import com.aliucord.coreplugins.voice.ui.codeBlock
import com.aliucord.coreplugins.voice.ui.collapsibleTitle
import com.aliucord.coreplugins.voice.ui.newCard
import com.aliucord.coreplugins.voice.ui.setCodeBlock
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.updater.ManagerBuild
import com.aliucord.utils.DimenUtils.dp
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.GsonUtils.fromJson
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.utils.accessField
import com.aliucord.utils.SemVer
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.wrappers.ChannelWrapper.Companion.guildId
import com.aliucord.wrappers.ChannelWrapper.Companion.id
import com.discord.api.stageinstance.StageInstance
import com.discord.api.stageinstance.StageInstancePrivacyLevel
import com.discord.play_delivery.PlayAssetDeliveryNativeWrapper
import com.discord.rtcconnection.RtcConnection
import com.discord.rtcconnection.mediaengine.MediaEngineConnection
import com.discord.rtcconnection.socket.io.Opcodes
import com.discord.rtcconnection.socket.io.Payloads
import com.discord.rtcconnection.socket.io.Payloads.Protocol.ProtocolInfo
import com.discord.api.voice.server.VoiceServer
import com.discord.native.engine.NativeConnection
import com.discord.native.engine.NativeEngine
import com.discord.stores.StoreApplicationStreaming
import com.discord.stores.StoreRtcConnection
import com.discord.stores.StoreMediaEngine
import com.discord.stores.StoreMediaSettings
import com.discord.stores.StoreStageInstances
import com.discord.stores.StoreStream
import com.discord.stores.StoreVoiceChannelSelected
import com.discord.stores.StoreVoiceChannelSelected.JoinVoiceChannelResult
import com.discord.stores.StoreVoiceParticipants
import com.discord.widgets.stage.StageChannelAPI
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.debug.DebugPrintBuilder
import com.discord.utilities.voice.VoiceChannelJoinability
import com.discord.utilities.voice.VoiceChannelJoinabilityUtils
import com.discord.widgets.settings.WidgetSettingsVoice
import com.discord.widgets.user.usersheet.UserProfileVoiceSettingsView
import com.discord.widgets.user.usersheet.WidgetUserSheet
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel
import com.discord.widgets.chat.list.entries.MessageEntry
import com.discord.widgets.stage.StageChannelNotifications
import com.discord.widgets.stage.sheet.WidgetStageModeratorJoinBottomSheet
import com.discord.widgets.stage.sheet.WidgetStageStartEventBottomSheetViewModel
import com.discord.widgets.stage.start.ModeratorStartStageViewModel
import com.discord.widgets.stage.start.WidgetModeratorStartStage
import com.discord.widgets.voice.controls.VoiceControlsSheetView
import com.discord.widgets.voice.fullscreen.CallParticipant
import com.discord.widgets.voice.fullscreen.WidgetCallFullscreen
import com.discord.widgets.voice.model.CallModel
import com.discord.widgets.voice.fullscreen.WidgetCallFullscreenViewModel
import com.discord.widgets.voice.fullscreen.WidgetCallPreviewFullscreenViewModel
import com.discord.widgets.voice.sheet.WidgetVoiceBottomSheetViewModel
import com.discord.widgets.voice.sheet.WidgetVoiceSettingsBottomSheet
import com.hammerandchisel.libdiscord.Discord
import com.lytefast.flexinput.R
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.io.File
import java.util.Collections
import java.util.Locale
import java.util.WeakHashMap
import b.a.q.m0.c.e as MediaEngineConnectionLegacy
import b.a.q.m0.c.`e$h` as MediaEngineConnectionLegacy_SetCodecs
import b.a.q.n0.a as RtcControlSocket
import b.a.q.n0.`a$j` as RtcControlSocket_OnMessage
import b.a.q.n0.`a$k` as RtcControlSocket_Connect

internal class VoiceChatFix : CorePlugin(Manifest("VoiceChatFix"))  {
    override val isHidden = false
    override val isRequired = true
    private val debugInfo = LinkedHashMap<String, String>()
    private var currentSocket: RtcControlSocket? = null
    private val epochPreparedSockets = Collections.newSetFromMap(WeakHashMap<RtcControlSocket, Boolean>())
    private val pendingProposals = WeakHashMap<RtcControlSocket, MutableList<ByteString>>()
    private var prevSocket: RtcControlSocket? = null
    @Volatile
    private var supportedModes: List<String>? = null

    // Show the join as speaker/audience bottom sheet after a start too since base
    // only shows it when joining a live stage
    private val pendingJoinAsPrompt = Collections.synchronizedSet(HashSet<Long>())

    private val libVersion = runCatching {
        Class.forName("com.aliucord.voice.BuildConfig")
            .getField("VERSION")
            .get(null) as String
    }.getOrNull()

    private var Payloads.Stream.maxFrameRateField by accessField<Int?>("maxFrameRate")
    private var WidgetCallFullscreenViewModel.channelIdField by accessField<Long>("channelId")
    private var Discord.nativeEngineField by accessField<NativeEngine?>("nativeEngine")
    private var Connection.nativeConnectionField by accessField<NativeConnection>("native")

    private companion object {
        // Native libs and webrtc dex are built together (aliuvoice aar)
        // the lib version must match exactly but injector & patches only need a min version
        // TODO: Injector check shouldn't be necessary,
        //  let's keep it here until this PR is ready to merge just in case we need it later
        val EXPECTED_LIB_VERSION = com.aliucord.voice.BuildConfig.VERSION
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
            "Implementation of DAVE, which supports E2EE voice, camera and screenshare support to Aliucord"
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
            if (mode == TransportModes.XSALSA20) {
                param.args[2] = if (libVersion != null) {
                    VoiceChatFixSettings.transportEncryption
                } else {
                    TransportModes.XSALSA20_LITE
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
        VoiceStatus.register(patcher)
        patchUserSheetView()
        Soundboard.register(context)
        patchSoundboardVolume()
        patchVoiceMoveReconnect()
        patchVoiceAccess()
        patchDaveEnforcement()
        patchSidechainCompression()
        patchCallStartTime()
        patchCallCardTicker()
        patchCallDurationText()
        patchSilenceUnhandledEvents(patcher)
        patchAutoAcceptSpeakInvite()
        patchStageStartFlow()
        patchStageJoinPromptOnStart()
        ModernAudioDevices.register(patcher)

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
                        stream.maxFrameRateField = VoiceChatFixSettings.videoFramerate
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
                    data = ProtocolInfo(base.data.address, base.data.port, mode)
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
                    val modes = JSONObject(message.data.toString()).optJSONArray("modes")
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
                // silence, useless opcode *huge explosion*
                Opcodes.HEARTBEAT_ACK -> { /* chainsaw man reference??? */ }
                else -> logger.warn("Unhandled Opcode: ${Opcodes.friendly(message.opcode)}")
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

            // We use the heartbeat event (triggered every ~13.75s) as a refresh for
            // the connection info, also the event replies with the sent at timestamp
            // providing a zero-overhead websocket ping
            if (message.opcode == Opcodes.HEARTBEAT_ACK && VoiceChatFixSettings.showConnInfo) {
                message.data.toString().trim('"').toLongOrNull()?.let { sent ->
                    setDebug("Ping", "${System.currentTimeMillis() - sent}ms")
                }

                refreshEngineStats(socket)
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
                    is VoiceChatFixPayload.ClientFlags -> {
                        logger.debug("ClientFlags: userId=${payload.userId} flags=${payload.flags}")
                    }
                    is VoiceChatFixPayload.ClientPlatform -> {
                        logger.debug("ClientPlatform: userId=${payload.userId} platform=${payload.platform}")
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
            currentSocket?.takeIf { it.rtcConnections.isNotEmpty() }?.let(::applyVideoSettings)
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
                // DAVE binary frames dispatch on a different path than JSON opcodes, so an inbound
                // PROPOSALS frame can be processed before SELECT_PROTOCOL_ACK prepares the epoch.
                // Queue it until the epoch is ready (handleOnProtocolSelectAck), then drain.
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
        setDebug("Resolution", "${VoiceChatFixSettings.videoWidth} x ${VoiceChatFixSettings.videoHeight} @ ${VoiceChatFixSettings.videoFramerate} fps")
    }

    private fun setDebug(key: String, value: String) {
        debugInfo[key] = value
        refreshConnInfo()
    }

    private fun renderDebugInfo(): String =
        debugInfo.entries.joinToString("\n") { "${it.key}:  ${it.value}" }

    private fun refreshConnInfo() {
        if (!VoiceChatFixSettings.showConnInfo) return

        val sb = StringBuilder()
        val head = renderDebugInfo()
        if (head.isNotEmpty()) sb.append(head).append("\n\n")
        // Fallback to the previous socket if the current one is inactive to keep the
        // overlay populated instead of going blank.
        val rtc = currentSocket.firstConnectionOrNull(prevSocket)

        if (rtc != null) {
            runCatching {
                val b = StringBuilder()
                rtc.debugPrint(DebugPrintBuilder(b))
                sb.append(b.toString().trim())
            }.onFailure { sb.append("(debugPrint failed: ${it.message})") }
        } else if (head.isEmpty()) {
            sb.append("Not connected")
        }

        sb.append("\n\n- Codec Capabilities:\n")
        sb.append(
            Discord.codecCapabilities.values
                .sortedBy { it.codec }
                .joinToString("\n") { "  + ${it.codec}: decode=${it.decode}, encode=${it.encode}" }
                .ifEmpty { "  - No codecs available" }
        )

        connInfoText = sb.toString().ifEmpty { "Not connected" }
        onConnInfoUpdate(connInfoText)
    }

    private fun refreshEngineStats(socket: RtcControlSocket) {
        socket.connections.forEach { connection ->
            runCatching {
                connection.nativeConnectionField.getStats { json ->
                    runCatching {
                        val stats = JSONObject(json)
                        val rtt = stats.optJSONObject("transport")?.optInt("rtt", -1) ?: -1

                        setDebug("RTT", if (rtt >= 0) "${rtt}ms" else "Unavailable")

                        val fractionLost = stats.optJSONObject("outbound")
                            ?.optJSONObject("audio")
                            ?.optDouble("fractionLost", 0.0) ?: 0.0

                        setDebug("Packed Loss", String.format(Locale.ROOT, "%.1f%%", fractionLost * 100))
                    }.onFailure {
                        logger.warn("Failed to parse engine stats: $it")
                    }
                }
            }.onFailure {
                logger.warn("getStats failed: $it")
            }
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
                        typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.sourcecodepro_semibold)
                        setBackgroundColor(Color.TRANSPARENT)
                        setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorTextNormal))
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
                        Utils.mainThread.post { info.setCodeBlock(text) }
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

    private fun patchUserSheetView() {
        patcher.before<WidgetUserSheet>(
            "configureVoiceSection",
            WidgetUserSheetViewModel.ViewState.Loaded::class.java,
        ) { (_, state: WidgetUserSheetViewModel.ViewState.Loaded) ->
            sheetUserId = state.user.id
        }

        patcher.after<UserProfileVoiceSettingsView>(
            "updateView",
            UserProfileVoiceSettingsView.ViewState::class.java,
        ) {
            try {
                addMuteSoundboardRow(this, sheetUserId)
                addDisableVideoRow(currentSocket, this, sheetUserId)
                addVerificationRow(currentSocket, this, sheetUserId)
            } catch (e: Throwable) {
                logger.error("Failed to add user sheet voice rows", e)
            }
        }

        patcher.after<WidgetCallFullscreenViewModel>(
            "createVideoGridEntriesForParticipant",
            StoreVoiceParticipants.VoiceUser::class.java,
            Long::class.javaPrimitiveType!!,
            StoreApplicationStreaming.ActiveApplicationStream::class.java,
            RtcConnection.Quality::class.java,
            VideoInputDeviceDescription::class.java,
            Boolean::class.javaPrimitiveType!!,
        ) { (param, participant: StoreVoiceParticipants.VoiceUser) ->
            try {
                if (isVideoDisabled(participant.user.id)) param.result = emptyList<CallParticipant>()
            } catch (e: Throwable) {
                logger.error("Failed to hide video stream from participant", e)
            }
        }
    }

    private fun patchVoiceMoveReconnect() {
        var lastServerUpdate = 0L

        val checkForVoiceServerUpdate = StoreRtcConnection::class.java
            .getDeclaredMethod("checkForVoiceServerUpdate")
            .apply { isAccessible = true }

        patcher.before<StoreRtcConnection>(
            "handleVoiceServerUpdate",
            VoiceServer::class.java,
        ) {
            lastServerUpdate = System.currentTimeMillis()
        }

        patcher.after<StoreRtcConnection>(
            "handleVoiceChannelSelected",
            Long::class.javaObjectType,
        ) { (_, channelId: Long?) ->
            if (channelId == null || (System.currentTimeMillis() - lastServerUpdate) > 3000) return@after

            logger.info("Voice server update preceded channel switch (user moved); re-checking endpoint")
            checkForVoiceServerUpdate.invoke(this)
        }
    }

    private fun patchVoiceAccess() {
        fun Long.joinBlockReason(result: JoinVoiceChannelResult? = null): String? {
            if (this == 0L) return null

            val joinability = when (result) {
                null -> VoiceChannelJoinabilityUtils.INSTANCE.getJoinability(this)
                JoinVoiceChannelResult.FAILED_PERMISSIONS_MISSING -> VoiceChannelJoinability.PERMISSIONS_MISSING
                JoinVoiceChannelResult.FAILED_CHANNEL_FULL -> VoiceChannelJoinability.CHANNEL_FULL
                JoinVoiceChannelResult.FAILED_GUILD_VIDEO_AT_CAPACITY -> VoiceChannelJoinability.GUILD_VIDEO_AT_CAPACITY
                JoinVoiceChannelResult.FAILED_CHANNEL_DOES_NOT_EXIST -> VoiceChannelJoinability.CHANNEL_DOES_NOT_EXIST
                else -> VoiceChannelJoinability.CAN_JOIN
            }

            val reason = when (joinability) {
                VoiceChannelJoinability.CAN_JOIN -> null
                VoiceChannelJoinability.PERMISSIONS_MISSING -> "You don't have permission to join this voice channel"
                VoiceChannelJoinability.CHANNEL_FULL -> "This voice channel is full"
                VoiceChannelJoinability.GUILD_VIDEO_AT_CAPACITY -> "This channel has reached its video participant limit"
                VoiceChannelJoinability.CHANNEL_DOES_NOT_EXIST -> "This voice channel no longer exists"
            } ?: return null

            logger.info("Blocked while trying to join voice chat: $reason")
            Utils.showToast(reason)
            return reason
        }

        // Voice channel bottom sheet ("Join Voice" button)
        patcher.before<WidgetVoiceBottomSheetViewModel>(
            "joinVoiceChannel",
            Long::class.javaPrimitiveType!!,
        ) { (param, channelId: Long) ->
            channelId.joinBlockReason() ?: return@before
            param.result = null
        }

        // Call preview screen, skipping the original also skips the LaunchVideoCall
        patcher.before<WidgetCallPreviewFullscreenViewModel>(
            "joinVoiceChannel",
            Long::class.javaPrimitiveType!!,
        ) { (param, channelId: Long) ->
            channelId.joinBlockReason() ?: return@before
            param.result = null
        }

        // Join button on the fullscreen call screen
        patcher.before<WidgetCallFullscreenViewModel>("tryConnectToVoice") { param ->
            channelIdField.joinBlockReason() ?: return@before
            updateViewState(WidgetCallFullscreenViewModel.ViewState.Invalid.INSTANCE)
            param.result = null
        }

        // Other missing checks to make sure
        patcher.after<StoreVoiceChannelSelected>(
            "selectVoiceChannelInternal",
            Long::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
        ) { (param, channelId: Long) ->
            channelId.joinBlockReason(param.result as JoinVoiceChannelResult)
        }
    }

    // Checking voice call with maxDaveProtocolVersion=0 if the websocket gets
    // closed with error 4017 ("E2EE/DAVE protocol required")
    // If triggered, we re-enable DAVE before the retry
    // Other close codes get surfaced as toasts (or logs for the routine ones)
    // instead of looking like silent drops
    private fun patchDaveEnforcement() {
        patcher.patch(
            RtcControlSocket::class.java.getDeclaredMethod(
                "a",
                RtcControlSocket::class.java,
                Boolean::class.javaPrimitiveType!!,
                Integer::class.java,
                String::class.java,
            ),
            PreHook { param ->
                val code = param.args[2] as? Int ?: return@PreHook
                val close = VoiceCloseCodes.from(code) ?: return@PreHook
                val reason = (param.args[3] as? String)?.takeIf { it.isNotEmpty() } ?: close.message

                if (close == VoiceCloseCodes.DAVE_REQUIRED) {
                    if (VoiceChatFixSettings.daveEnabled) {
                        logger.warn("RtcControlSocket closed with ${close.friendly()} despite DAVE enabled, unsupported protocol version?")
                        return@PreHook
                    }

                    var setting by VoiceChatFixSettings.daveEnabledDelegate
                    setting = true

                    logger.info("RtcControlSocket closed with ${close.friendly()}, re-enabling DAVE before retry")
                    Utils.mainThread.post {
                        Utils.showToast("VoiceChatFix: This call requires E2EE/DAVE to be enabled! The protocol has been automatically re-enabled", true)
                    }

                    return@PreHook
                }

                // If the preferred transport mode AES-256-GCM is rejected,
                // use XChaCha20 before trying again
                if (close == VoiceCloseCodes.UNKNOWN_ENCRYPTION_MODE && VoiceChatFixSettings.useAes256Gcm) {
                    var setting by VoiceChatFixSettings.useAes256GcmDelegate
                    setting = false

                    logger.warn("RtcControlSocket closed with ${close.friendly()}, disabling AES-256-GCM and falling back to XChaCha20")
                    Utils.mainThread.post {
                        Utils.showToast("VoiceChatFix: Server rejected AES-256-GCM, falling back to XChaCha20", true)
                    }

                    return@PreHook
                }

                if (close.toast) {
                    logger.warn("RtcControlSocket closed with ${close.friendly()}: '$reason'")
                    Utils.mainThread.post {
                        Utils.showToast("VoiceChatFix: ${close.message} ($code)", true)
                    }
                } else {
                    logger.info("RtcControlSocket closed with ${close.friendly()}: '$reason'")
                }
            }
        )
    }

    // On voice/Discord.kt this is forced to false, this is so we can enable the compression back
    // Only applied once when the native engine is constructed,
    // so switching this setting requires a restart to take effect
    private fun patchSidechainCompression() {
        patcher.after<Discord>(Context::class.java, Int::class.javaPrimitiveType!!) {
            val compress = VoiceChatFixSettings.sidechainCompression
            val engine = nativeEngineField ?: return@after
            engine.setSidechainCompression(compress)
            logger.debug("setSidechainCompression: $compress (user override)")
        }
    }

    // Track when the user receives and invite to speak
    // When triggered, automatically accept the invite
    private fun patchAutoAcceptSpeakInvite() {
        patcher.before<StageChannelNotifications>(
            "onInvitedToSpeak",
            Long::class.javaPrimitiveType!!)
        { (_, channelId: Long) ->
            if (!VoiceChatFixSettings.autoAcceptSpeakInvite) return@before
            
            logger.debug("Auto-accepting invite to speak in channel $channelId")
            StageChannelAPI.INSTANCE.ackInvitationToSpeak(channelId, true)?.subscribe { /* stage filled with cats */ }
        }
    }

    // When opening a unstarted (not live) stage and the user has staff permissions,
    // it shows 2 bottom sheets at the same time, overlapping one to change the topic
    // and staff the join as speaker/audience prompt
    private fun patchStageStartFlow() = runCatching {
        val stageClass = WidgetModeratorStartStage::class.java

        // configureUi re-runs on every ViewState update :sob:
        val launchStageCall = stageClass.getDeclaredMethod("launchStageCall", Long::class.javaPrimitiveType!!)
            .apply { isAccessible = true }
        val getChannelId = stageClass.getDeclaredMethod("getChannelId")
            .apply { isAccessible = true }

        patcher.patch(
            stageClass.getDeclaredMethod("configureUi", ModeratorStartStageViewModel.ViewState::class.java),
            PreHook { param ->
                launchStageCall.invoke(param.thisObject, getChannelId.invoke(param.thisObject))

                param.result = null
            })
    }.onFailure {
        logger.error("Failed to patch stage moderator start flow", it)
    }

    private fun patchStageJoinPromptOnStart() = runCatching {
        // Base has this horrid flow:
        // onCreate success => if mic permission granted => setSelfSpeaker() - unmuted btw
        patcher.before<WidgetStageStartEventBottomSheetViewModel>("setSelfSpeaker") {
            logger.debug("setSelfSpeaker: Blocked auto joining as speaker on stage start")
            it.result = null
        }

        // startStageInstance is only ever called by the local user starting a stage
        patcher.before<StageChannelAPI>(
            "startStageInstance",
            Long::class.javaPrimitiveType!!,
            String::class.java,
            StageInstancePrivacyLevel::class.java,
            Boolean::class.javaPrimitiveType!!,
            String::class.java,
        ) { (_, channelId: Long) ->
            pendingJoinAsPrompt.add(channelId)
        }

        // Once the instance loads and gets called, show the staff bottom sheet
        // asking the user to pick join as speaker or join as audience
        patcher.after<StoreStageInstances>(
            "handleStageInstanceCreate",
            StageInstance::class.java,
        ) { (_, instance: StageInstance) ->
            val channelId = instance.a()
            if (!pendingJoinAsPrompt.remove(channelId)) return@after

            Utils.mainThread.post {
                runCatching {
                    WidgetStageModeratorJoinBottomSheet.Companion!!.show(
                        Utils.appActivity.supportFragmentManager,
                        channelId
                    )
                }.onSuccess {
                    logger.debug("Showing moderator join sheet")
                }.onFailure {
                    logger.error("Failed to show moderator join sheet on start", it)
                }
            }
        }
    }.onFailure {
        logger.error("Failed to patch stage join prompt on start", it)
    }

    // Base aliucord doesn't handle VOICE_CHANNEL_START_TIME_UPDATE (completely missing)
    // On Discord RN it's the "call started ... ago"
    // Both events only fire on changes, so the initial values are fetched
    // via Opcode 43 REQUEST_CHANNEL_INFO on guild voice channel join
    private fun patchCallStartTime() {
        GatewayAPI.onEvent<VoiceChannelStartTime>("VOICE_CHANNEL_START_TIME_UPDATE") { update ->
            logger.debug("GatewayEvent[VOICE_CHANNEL_START_TIME_UPDATE]: $update")
            if (update.id == null) return@onEvent

            trackCallStart(update.id, update.voiceStartTime)
        }

        GatewayAPI.onEvent<ChannelInfo>("CHANNEL_INFO") { info ->
            logger.debug("GatewayEvent[CHANNEL_INFO]: $info")

            info.channels?.forEach { entry ->
                val id = entry.id ?: return@forEach

                trackCallStart(id, entry.voiceStartTime)
                VoiceStatus.track(id, entry.status)
            }
        }

        // Seed the initial values on guild voice channel join
        patcher.after<StoreVoiceChannelSelected>(
            "selectVoiceChannelInternal",
            Long::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
        ) { (param, channelId: Long) ->
            if (channelId <= 0L || param.result != JoinVoiceChannelResult.SUCCESS) return@after
            val guildId = StoreStream.getChannels().getChannel(channelId)?.guildId ?: return@after

            requestChannelInfo(guildId)
        }

        // Base leaves StoreVoiceChannelSelected.timeSelectedMs at 0 in some join flows (ie "Ongoing Call" in DMs)
        // Then because CallModel.timeConnectedMs is 0, it shows the call duration as "1/1/1970"
        patcher.after<StoreVoiceChannelSelected>("getTimeSelectedMs") { param ->
            param.result = backstopTimeSelected(currentSocket, param.result as? Long)
        }

        // Joining via the chat card's "Ongoing Call" never passes the timestamp field
        // Joining via the top right call button does
        // Without it, the call duration still shows as "1/1/1970"
        runCatching {
            Class.forName($$"com.discord.stores.StoreVoiceChannelSelected$observeTimeSelectedMs$1")
                .declaredMethods.filter { it.name == "invoke" }
                .forEach { method ->
                    patcher.patch(method, Hook { param ->
                        param.result = backstopTimeSelected(currentSocket, param.result as? Long)
                    })
                }
        }.onFailure {
            logger.error("Failed to patch observeTimeSelectedMs", it)
        }
    }

    // The "Ongoing Call" card in the chat DMs chat card ticks from the parsed timestamp
    // This doesn't take into account timezones or non-UTC devices
    // Change it into a ticker when the gateway told us the real timestamp
    // of when the call started
    private fun patchCallCardTicker() = runCatching {
        val itemClass = Class.forName("com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemCallMessage")
        val tickerClass = Class.forName($$"com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemCallMessage$configureSubtitle$1")
        val pendingCardChannel = ThreadLocal<Long?>()

        patcher.patch(
            itemClass.declaredMethods.first { it.name == "configureSubtitle" },
            PreHook { param ->
                val entry = param.args[0] as? MessageEntry ?: return@PreHook

                pendingCardChannel.set(entry.message.channelId)
            }
        )

        patcher.patch(
            tickerClass.getDeclaredConstructor(itemClass, Long::class.javaPrimitiveType),
            PreHook { param ->
                val channelId = pendingCardChannel.get() ?: return@PreHook
                pendingCardChannel.set(null)
                val start = callStartTimes[channelId] ?: return@PreHook
                logger.debug("Call card ticker: overriding start ${param.args[1]} to $start for channel $channelId")

                param.args[1] = start
            }
        )
    }.onFailure {
        logger.error("Failed to patch call card ticker", it)
    }

    // Expands the DM call duration line
    private fun patchCallDurationText() = runCatching {
        val lambdaClass = Class.forName($$"com.discord.widgets.voice.fullscreen.WidgetCallFullscreen$configureConnectionStatusText$1")

        patcher.patch(
            lambdaClass.declaredMethods.first { it.name == "invoke" && it.parameterTypes.singleOrNull() == Long::class.javaObjectType },
            Hook { param ->
                runCatching {
                    val callWidget = lambdaClass.getDeclaredField($$"this$0")
                        .apply { isAccessible = true }
                        .get(param.thisObject) as WidgetCallFullscreen

                    val callWidgetView = callWidget.view ?: return@Hook

                    val callModel = lambdaClass.getDeclaredField($$"$callModel")
                        .apply { isAccessible = true }
                        .get(param.thisObject) as CallModel

                    // The "Ongoing Call" card in the chat DMs never touches the stored timestamp,
                    // only emits once at subscribe time (before we even have a channelId) and freezes at 0 forever
                    // We ignore it completely and just replace it with our own custom timer(s)
                    val start = callModel.timeConnectedMs.takeIf { it > 0L }
                        ?: callModel.channel?.id?.let { callStartTimes.getOrPut(it) { System.currentTimeMillis() } }
                        ?: return@Hook

                    val timers = callTimersLines(start)

                    callWidgetView.findViewById<TextView>(
                        Utils.getResId("private_call_status_duration", "id")
                    )?.apply {
                        isSingleLine = false
                        maxLines = timers.size
                        textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                        this.text = timers.joinToString("\n")
                    }
                }.onFailure {
                    logger.error("Failed to expand call duration text", it)
                }
            }
        )
    }.onFailure {
        logger.error("Failed to patch call duration text", it)
    }

    private fun patchSoundboardVolume() {
        val labelId = View.generateViewId()
        val sliderId = View.generateViewId()
        patcher.after<WidgetSettingsVoice>(
            "configureUI",
            WidgetSettingsVoice.Model::class.java,
        ) {
            val binding = WidgetSettingsVoice.`access$getBinding$p`(this)
            val volumeBar = binding.s
            val parent = volumeBar.parent as? LinearLayout ?: return@after
            if (parent.findViewById<View?>(sliderId) != null) return@after

            val ctx = parent.context
            val index = parent.indexOfChild(volumeBar)

            TextView(ctx, null, 0, R.i.UiKit_Settings_Base_Item).apply {
                id = labelId
                text = "Soundboard Volume"
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    topMargin = 12.dp
                }
                parent.addView(this, index + 1)
            }

            SeekBar(ctx, null, 0, R.i.UiKit_SeekBar).apply {
                id = sliderId
                max = 100
                progress = VoiceChatFixSettings.soundboardVolume.coerceIn(0, 100)
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                setPadding(volumeBar.paddingLeft, volumeBar.paddingTop, volumeBar.paddingRight, volumeBar.paddingBottom)

                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {}
                    override fun onStartTrackingTouch(sb: SeekBar?) {}
                    override fun onStopTrackingTouch(sb: SeekBar?) {
                        var setting by VoiceChatFixSettings.soundboardVolumeDelegate
                        setting = sb?.progress ?: return
                    }
                })
                parent.addView(this, index + 2)
            }
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

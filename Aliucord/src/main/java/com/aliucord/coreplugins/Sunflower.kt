package com.aliucord.coreplugins

import android.content.Context
import co.discord.media_engine.Connection
import com.aliucord.Logger
import com.aliucord.coreplugins.voice.SunflowerPayload
import com.aliucord.coreplugins.voice.SunflowerPayload.DaveTransitionReady
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.SerializedName
import com.discord.native.engine.NativeConnection
import com.discord.rtcconnection.RtcConnection
import com.discord.rtcconnection.socket.io.Opcodes
import com.discord.rtcconnection.socket.io.Payloads
import com.discord.rtcconnection.socket.io.Payloads.Protocol.ProtocolInfo
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import b.a.q.h0 as RtcConnectionEventHandler
import b.a.q.m0.c.e as MediaEngineConnectionLegacy
import b.a.q.n0.a as RtcControlSocket
import b.a.q.n0.`a$j` as RtcControlSocket_OnMessage
import f0.e0.n.d as RealWebsocket

@Suppress("NOTHING_TO_INLINE")
inline fun RtcControlSocket.send(data: SunflowerPayload.Outgoing) = n(data.opcode, data)

// Ref: https://github.com/square/okhttp/blob/c7556e0ac6d690ccb71d304d22d636f2f86baf7b/okhttp/src/commonJvmAndroid/kotlin/okhttp3/internal/ws/RealWebSocket.kt#L434-L456
// The real send function was inlined into the text one @ RealWebsocket.a()
// So we have to copy and adapt it to send ordinary ByteStrings
fun RtcControlSocket.send(data: ByteString): Boolean {
    val websocket = this.s as RealWebsocket
    websocket.run {
        synchronized(this) {
            if (!this.p && !this.m) {
                if (this.l + data.j() > 16L * 1024 * 1024) {
                    e(1001, null) // close(code = 1001, reason = null)
                    return false
                }
                this.l += data.j()
                this.k.add(RealWebsocket.b(2, data)) // 2 = OPCODE_BINARY
                l()
                return true
            }
            return false
        }
    }
}

fun RtcControlSocket.send(opcode: Int, data: ByteString) {
    // Prepend the opcode as the first byte before the data
    val bytes = arrayOf(opcode.toByte()).toByteArray() + data.i()
    Logger("Sunflower").info("Sending opcode $opcode: ${ByteString(bytes).f()} ")
    send(ByteString(bytes))
}

open class BTest {
    fun xd1() {
        Logger("Sunflower").info("BTest unpatched")
    }
}

class Test1 : BTest()
class Test2 : BTest()
class Test3 : BTest()

data class NewIdentify(
    @SerializedName("server_id") val serverId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("session_id") val sessionId: String,
    val token: String,
    @SerializedName("max_dave_protocol_version") val maxDaveProtocolVersion: Int,
)

internal class Sunflower : CorePlugin(Manifest("Sunflower"))  {
    override val isHidden = true
    override val isRequired = true

    init {
        manifest.description = "Fixes VC not working properly."
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    val RtcControlSocket.rtcConnection: RtcConnection? get() {
        val connections = this.q
            .mapNotNull { eventHandler ->
                val eventHandler = eventHandler as? RtcConnectionEventHandler
                if (eventHandler == null) {
                    logger.error("ConnectionEventHandler failed to cast", null)
                    return@mapNotNull null
                }

                eventHandler.a
            }
            .distinct()

        if (connections.isEmpty()) {
            logger.warn("No rtcconnection found")
        } else if (connections.size > 1) {
            logger.warn("More than one rtcconnection found, using the first one")
        }
        return connections.getOrNull(0)
    }

    val RtcControlSocket.connections: List<Connection> get() {
        val connections = this.q
            .mapNotNull { eventHandler ->
                val eventHandler = eventHandler as? RtcConnectionEventHandler
                if (eventHandler == null) {
                    logger.error("ConnectionEventHandler failed to cast", null)
                    return@mapNotNull null
                }

                val engine = eventHandler.a.x as MediaEngineConnectionLegacy
                engine.j
                // val engine = eventHandler.a.U
                // engine.connections
                //     .mapNotNull { conn ->
                //         val connection = conn as? e
                //         if (connection == null) {
                //             logger.error("Connection failed to cast", null)
                //             return@mapNotNull null
                //         }
                //         connection.j
                //     }
            }
            // .flatten()
            .distinct()

        if (connections.isEmpty()) {
            logger.warn("No connection found whilst handling event")
        } else if (connections.size > 1) {
            logger.warn("More than one connection found, passing event to all")
        }
        return connections
    }

    override fun start(context: Context) {
        // Force usage of updated transport encryption
        // TODO: Ideally prefer "aead_aes256_gcm_rtpsize" instead somehow (not all voice servers support it)
        patcher.before<ProtocolInfo>(
            String::class.java,
            Int::class.javaPrimitiveType!!,
            String::class.java,
        ) { (param, _: String, _: Int, mode: String) ->
            if (mode == "xsalsa20_poly1305") {
                param.args[2] = "aead_xchacha20_poly1305_rtpsize"
            }
        }

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

        patcher.before<Payloads.Outgoing>(Int::class.javaPrimitiveType!!, Any::class.java) { (param, opcode: Int, data: Any) ->
            if (opcode == 0) {
                val d = data as Payloads.Identify
                param.args[1] = NewIdentify(
                    serverId = d.serverId,
                    userId = d.userId.toString(),
                    sessionId = d.sessionId,
                    token = d.token,
                    maxDaveProtocolVersion = 1
                )
                logger.info("Replacing Identify payload")
                logger.info("Before: $d")
                logger.info("After: ${param.args[1]}")
            }
        }

        // Handle new voice gateway events
        patcher.after<RtcControlSocket_OnMessage>("invoke") { param ->
            val socket: RtcControlSocket = this.`this$0`

            if (socket.s != this.`$webSocket`) {
                param.result = Unit.a
                return@after
            }

            val message = this.`$message`
            val gson = socket.n

            if (message.opcode == Opcodes.READY) {
                handleOnHello(socket)
            }
            if (message.opcode == Opcodes.SELECT_PROTOCOL_ACK) {
                val ver = message.data.d().a["secure_frames_version"]?.e()?.c() ?: 0
                logger.info("Protover: $ver")
                handleOnProtocolSelect(socket, ver)
            }

            val payload = SunflowerPayload.deserialize(gson, message)
                ?: return@after
            logger.debug("Sunflower payload ${message.opcode}: $payload")

            socket.connections.forEach { connection ->
                when (payload) {
                    // is SunflowerPayload.ClientsConnect -> {}
                    is SunflowerPayload.DavePrepareTransition -> {
                        connection.prepareSecureFramesTransition(
                            transitionId = payload.transitionId,
                            protocolVersion = payload.protocolVersion,
                            callback = object : NativeConnection.SecureFramesTransitionReadyCallback {
                                override fun onTransitionReady() {
                                    socket.send(DaveTransitionReady(payload.transitionId))
                                }
                            }
                        )
                    }
                    is SunflowerPayload.DaveExecuteTransition -> {
                        connection.executeSecureFramesTransition(payload.transitionId)
                    }
                    is SunflowerPayload.DavePrepareEpoch -> {
                        // TODO: transitionId should be grabbed from somewhere, maybe in secure frames callback's version?
                        connection.prepareSecureFramesEpoch(
                            epoch = payload.epoch.toString(),
                            transitionId = payload.epoch,
                            groupId = socket.rtcConnection?.i()?.c?.toString() ?: "" // Channel ID
                        )
                        connection.getMLSKeyPackageB64(object : NativeConnection.MLSKeyPackageCallback {
                            override fun onMLSKeyPackage(keyPackageB64: String) {
                                val bytes = keyPackageB64.decodeBase64ToArray()
                                logger.debug("Received MLS Key package, sending over")
                                socket.send(Opcodes.DAVE_MLS_KEY_PACKAGE, ByteString(bytes))
                            }
                        })
                        // nConn.prepareSecureFramesEpoch()
                    }
                }
            }
        }
    }

    // Upon protocol selection, start preparing for dave
    private fun handleOnHello(socket: RtcControlSocket) {
        val channelId = socket.rtcConnection?.i()?.c
            ?: return logger.warn("No rtc connection upon hello")
        socket.connections.forEach { connection ->
            // logger.debug("Hello! Preparing secure frames epoch for $channelId")
            // connection.native.prepareSecureFramesEpoch("1", 1, channelId.toString())
        }
    }
    private fun handleOnProtocolSelect(socket: RtcControlSocket, version: Int) {
        val channelId = socket.rtcConnection?.i()?.c
            ?: return logger.warn("No rtc connection upon protocol select ack")
        socket.connections.forEach { connection ->
            val channelId = socket.rtcConnection?.i()?.c
                ?: return logger.warn("No rtc connection found whilst preparing secure frames")
            if (version == 0) {
                logger.debug("No secure frames, bye!")
                connection.prepareSecureFramesTransition(0, 0, object : NativeConnection.SecureFramesTransitionReadyCallback {
                    override fun onTransitionReady() {
                        logger.debug("Transitioned to secure frame ver 0")
                    }
                })
                return@forEach
            }
            logger.debug("Preparing secure frames epoch..")
            connection.prepareSecureFramesEpoch("1", 1, channelId.toString())
            logger.debug("Grabbing MLS Key..")
            connection.getMLSKeyPackageB64(object : NativeConnection.MLSKeyPackageCallback {
                override fun onMLSKeyPackage(keyPackageB64: String) {
                    val bytes = keyPackageB64.decodeBase64ToArray()
                    logger.debug("Received MLS Key package, sending over")
                    socket.send(Opcodes.DAVE_MLS_KEY_PACKAGE, ByteString(bytes))
                }
            })
        }
    }

    private fun handleBinaryMessage(socket: RtcControlSocket, bytestr: ByteString) {
        logger.info("Received binary message ${bytestr.f()}") //encodeBase64
        val bytes = bytestr.i()
        // First byte is the opcode, this is contrary to most docs because we are using an older version
        // of the voice gateway without resuming.
        // On newer versions, the first two bytes denote the sequence, and the third one is the opcode.
        // The sequence number is not present on old voice gateway versions.
        val opcode = bytes[0].toInt()

        // val i = bytes.iterator()

        if (opcode == Opcodes.DAVE_MLS_EXTERNAL_SENDER) {
            val nBytestr = ByteString(bytes.drop(1).toByteArray())
            logger.info("MLSExternalSender: ${nBytestr.f()}") //encodeBase64
            socket.connections.forEach { connection ->
                connection.updateMLSExternalSenderB64(nBytestr.f())
            }
        }
        if (opcode == Opcodes.DAVE_MLS_PROPOSALS) {
            val nBytestr = ByteString(bytes.drop(1).toByteArray())
            logger.info("MLSProposals: ${nBytestr.f()}") //encodeBase64
            socket.connections.forEach { connection ->
                // Utils.threadPool.execute {
                    // Thread.sleep(500)
                    connection.processMLSProposalsB64(nBytestr.f(), object : NativeConnection.MLSProcessProposalsCallback {
                        override fun onMLSCommitWelcome(commitWelcome: String) {
                            logger.info("MLSProposals commit received, sending over..: $commitWelcome") //encodeBase64
                            socket.send(Opcodes.DAVE_MLS_COMMIT_WELCOME, ByteString(commitWelcome.decodeBase64ToArray()))
                        }
                    })
                // }
            }
        }
        if (opcode == Opcodes.DAVE_MLS_ANNOUNCE_COMMIT_TRANSITION) {
            val nBytestr = ByteString(bytes.drop(3).toByteArray()) // An extra 2, sometimes 4, bytes need to be removed.
            logger.info("MLSAnnounceCommitTransition: ${nBytestr.f()}") //encodeBase64
            socket.connections.forEach { connection ->
                connection.prepareMLSCommitTransitionB64(0, nBytestr.f(), object : NativeConnection.MLSCommitTransitionCallback {
                    override fun onMLSProcessedCommit(processedCommit: Boolean, protocolVersion: Int, rosterChange: String) {
                        logger.info("MLSAnnounceCommitTransition processed: $processedCommit, ver: ${protocolVersion}, changes: $rosterChange")
                    }
                })
            }
        }
        if (opcode == Opcodes.DAVE_MLS_WELCOME) {
            val nBytestr = ByteString(bytes.drop(3).toByteArray()) // An extra 2 bytes need to be removed. Why? idk
            logger.info("MLSWelcome: ${nBytestr.f()}") //encodeBase64
            socket.connections.forEach { connection ->
                connection.processMLSWelcomeB64(0, nBytestr.f(), object : NativeConnection.MLSWelcomeCallback {
                    override fun onMLSProcessedWelcome(joinedGroup: Boolean, protocolVersion: Int, rosterChange: String) {
                        logger.info("MLSWelcome Processed, joined: $joinedGroup, ver: $protocolVersion, changes: $rosterChange")
                    }
                })
            }
        }
    }
}


// Method was nuked by dead code removal, so we are copying and bringing it back
// https://github.com/square/okio/blob/50abe8900f2e7bd48d4afc77bda0afd74fc790ac/okio/src/commonMain/kotlin/okio/Base64.kt
// Cheers ^w^
internal fun String.decodeBase64ToArray(): ByteArray? {
    // Ignore trailing '=' padding and whitespace from the input.
    var limit = length
    while (limit > 0) {
        val c = this[limit - 1]
        if (c != '=' && c != '\n' && c != '\r' && c != ' ' && c != '\t') {
            break
        }
        limit--
    }

    // If the input includes whitespace, this output array will be longer than necessary.
    val out = ByteArray((limit * 6L / 8L).toInt())
    var outCount = 0
    var inCount = 0

    var word = 0
    var pos = -1
    while (pos < limit - 1) {
        pos++
        val c = this[pos]

        val bits: Int
        if (c in 'A'..'Z') {
            // char ASCII value
            //  A    65    0
            //  Z    90    25 (ASCII - 65)
            bits = c.code - 65
        } else if (c in 'a'..'z') {
            // char ASCII value
            //  a    97    26
            //  z    122   51 (ASCII - 71)
            bits = c.code - 71
        } else if (c in '0'..'9') {
            // char ASCII value
            //  0    48    52
            //  9    57    61 (ASCII + 4)
            bits = c.code + 4
        } else if (c == '+' || c == '-') {
            bits = 62
        } else if (c == '/' || c == '_') {
            bits = 63
        } else if (c == '\n' || c == '\r' || c == ' ' || c == '\t') {
            continue
        } else {
            return null
        }

        // Append this char's 6 bits to the word.
        word = word shl 6 or bits

        // For every 4 chars of input, we accumulate 24 bits of output. Emit 3 bytes.
        inCount++
        if (inCount % 4 == 0) {
            out[outCount++] = (word shr 16).toByte()
            out[outCount++] = (word shr 8).toByte()
            out[outCount++] = word.toByte()
        }
    }

    val lastWordChars = inCount % 4
    when (lastWordChars) {
        1 -> {
            // We read 1 char followed by "===". But 6 bits is a truncated byte! Fail.
            return null
        }
        2 -> {
            // We read 2 chars followed by "==". Emit 1 byte with 8 of those 12 bits.
            word = word shl 12
            out[outCount++] = (word shr 16).toByte()
        }
        3 -> {
            // We read 3 chars, followed by "=". Emit 2 bytes for 16 of those 18 bits.
            word = word shl 6
            out[outCount++] = (word shr 16).toByte()
            out[outCount++] = (word shr 8).toByte()
        }
    }

    // If we sized our out array perfectly, we're done.
    if (outCount == out.size) return out

    // Copy the decoded bytes to a new, right-sized array.
    return out.copyOf(outCount)
}

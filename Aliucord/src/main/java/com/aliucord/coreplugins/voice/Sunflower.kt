package com.aliucord.coreplugins.voice

import android.content.Context
import com.aliucord.coreplugins.voice.SunflowerPayload.DaveTransitionReady
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.SerializedName
import com.discord.rtcconnection.socket.io.Opcodes
import com.discord.rtcconnection.socket.io.Payloads
import com.discord.rtcconnection.socket.io.Payloads.Protocol.ProtocolInfo
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import b.a.q.n0.a as RtcControlSocket
import b.a.q.n0.`a$j` as RtcControlSocket_OnMessage

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
        manifest.description = "Adds support for end-to-end encrypted voice chat"
    }

    override fun stop(context: Context) = patcher.unpatchAll()

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

        // During payload creation, if it is an Identify payload, replace it with a new one
        // that contains the dave protocol version
        patcher.before<Payloads.Outgoing>(
            Int::class.javaPrimitiveType!!,
            Any::class.java
        ) { (param, opcode: Int, data: Any) ->
            if (opcode == 0) {
                val d = data as Payloads.Identify
                param.args[1] = NewIdentify(
                    serverId = d.serverId,
                    userId = d.userId.toString(),
                    sessionId = d.sessionId,
                    token = d.token,
                    maxDaveProtocolVersion = 1
                )
                logger.debug("Replacing Identify payload")
                logger.debug("Before: $d")
                logger.debug("After: ${param.args[1]}")
            }
        }

        // Handle new (json) voice gateway events
        patcher.after<RtcControlSocket_OnMessage>("invoke") { param ->
            val socket: RtcControlSocket = this.`this$0`

            // Not sure what this check is for but it's done in original code
            if (socket.s != this.`$webSocket`) {
                param.result = Unit.a
                return@after
            }

            val message = this.`$message`
            val gson = socket.n

            if (message.opcode == Opcodes.SELECT_PROTOCOL_ACK) {
                // message.data.jsonObject.entries["secure_frames_version"]?.jsonPrimitive?.asInt()
                val ver = message.data.d().a["secure_frames_version"]?.e()?.c() ?: 0
                logger.debug("Protover: $ver")
                handleOnProtocolSelectAck(socket, ver)
            }

            val payload = SunflowerPayload.deserialize(gson, message)
                ?: return@after
            logger.debug("Sunflower payload ${Opcodes.friendly(message.opcode)}: $payload")

            socket.connections.forEach { connection ->
                when (payload) {
                    is SunflowerPayload.DavePrepareTransition -> {
                        connection.prepareSecureFramesTransition(
                            transitionId = payload.transitionId,
                            protocolVersion = payload.protocolVersion
                        ) {
                            socket.send(DaveTransitionReady(payload.transitionId))
                        }
                    }
                    is SunflowerPayload.DaveExecuteTransition -> {
                        connection.executeSecureFramesTransition(payload.transitionId)
                    }
                    is SunflowerPayload.DavePrepareEpoch -> {
                        // TODO: transitionId should be grabbed from somewhere, maybe in secure frames callback's version?
                        connection.prepareSecureFramesEpoch(
                            epoch = payload.epoch.toString(),
                            transitionId = payload.epoch,
                            // socket.rtcConnection?.getMetadata()?.channelId?.toString()
                            groupId = socket.rtcConnection?.i()?.c?.toString() ?: ""
                        )
                        connection.getMLSKeyPackageB64 { keyPackageB64 ->
                            val bytes = keyPackageB64.decodeBase64ToArray()
                            logger.debug("Received MLS Key package, sending over")
                            socket.send(Opcodes.DAVE_MLS_KEY_PACKAGE, ByteString(bytes))
                        }
                    }
                }
            }
        }
    }

    // Upon protocol selection ack, start preparing for dave
    private fun handleOnProtocolSelectAck(socket: RtcControlSocket, version: Int) {
        // socket.rtcConnection?.getMetadata()?.channelId
        val channelId = socket.rtcConnection?.i()?.c
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
            logger.debug("Preparing secure frames epoch..")
            // TODO: Are these values correct?
            connection.prepareSecureFramesEpoch("1", 1, channelId.toString())
            logger.debug("Grabbing MLS Key..")
            connection.getMLSKeyPackageB64 { keyPackageB64 ->
                val bytes = keyPackageB64.decodeBase64ToArray()
                    // XXX: This should never happen, but if it does, does this handle gracefully?
                    ?: throw IllegalArgumentException("MLS Key Package from native binary undecodable")
                logger.debug("Received MLS Key package, sending over")
                socket.send(Opcodes.DAVE_MLS_KEY_PACKAGE, ByteString(bytes))
            }
        }
    }

    private fun handleBinaryMessage(socket: RtcControlSocket, bytestr: ByteString) {
        logger.debug("Received binary message ${bytestr.f()}") //encodeBase64
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
                reader.read(2) // An extra 2, sometimes 4, bytes need to be removed. When and why? idk TODO
                val encoded = reader.collectAsByteString().encodeBase64()
                logger.debug("MLSAnnounceCommitTransition: $encoded") //encodeBase64
                socket.connections.forEach { connection ->
                    // TODO: transitionId is from binary payload
                    connection.prepareMLSCommitTransitionB64(0, encoded) { processedCommit, protocolVersion, rosterChange ->
                        logger.debug("MLSAnnounceCommitTransition processed: $processedCommit, ver: $protocolVersion, changes: $rosterChange")
                    }
                }
            }
            Opcodes.DAVE_MLS_WELCOME -> {
                // An extra 2 bytes need to be removed. Why? idk yet TODO
                reader.read(2)
                val encoded = reader.collectAsByteString().encodeBase64()
                logger.debug("MLSWelcome: $encoded")
                socket.connections.forEach { connection ->
                    // TODO: transitionId is from binary payload
                    connection.processMLSWelcomeB64(0, encoded) { joinedGroup, protocolVersion, rosterChange ->
                        logger.debug("MLSWelcome Processed, joined: $joinedGroup, ver: $protocolVersion, changes: $rosterChange")
                    }
                }
            }
        }
    }
}

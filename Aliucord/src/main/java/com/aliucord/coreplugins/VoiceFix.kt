package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.rtcconnection.socket.io.Opcodes
import com.discord.rtcconnection.socket.io.Payloads.Protocol.ProtocolInfo

internal class VoiceFix : CorePlugin(Manifest("VoiceFix"))  {
    override val isHidden = true
    override val isRequired = true

    init {
        manifest.description = "Fixes VC not working properly."
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

        // Handle new voice gateway events
        patcher.after<b.a.q.n0.`a$j`>("invoke") { param ->
            if (this.`this$0`.s != this.`$webSocket`) {
                param.result = Unit.a
                return@after
            }

            val payload = this.`$message`
            val gson = this.`this$0`.n
            val connections = this.`this$0`.q
                .mapNotNull { eventHandler ->
                    val eventHandler = eventHandler as? b.a.q.h0
                    if (eventHandler == null) {
                        logger.error("ConnectionEventHandler failed to cast", null)
                        return@mapNotNull null
                    }

                    val engine = eventHandler.a.U
                    engine.connections
                        .mapNotNull { conn ->
                            val connection = conn as? b.a.q.m0.c.e
                            if (connection == null) {
                                logger.error("Connection failed to cast", null)
                                return@mapNotNull null
                            }
                            connection.j
                        }
                }
                .flatten()
                .distinct()

            var handled = true
            when (payload.opcode) {
                Opcodes.CLIENTS_CONNECT -> {
                    // val payload = gson.fromJson(payload.data, Voice2Payloads.ClientsConnect::class.java)
                    // payload.userIds.forEach { id ->
                    //     connections.forEach { conn ->
                    //         conn.connectUser()
                    //     }
                    // }
                }
                Opcodes.CLIENT_DISCONNECT -> {
                    // connections.forEach { it.disconnectUser() }
                    //
                    // Kt does some extra stuff with this as well
                    handled = false
                }
                // TODO: davee
                Opcodes.DAVE_PREPARE_TRANSITION -> {}
                Opcodes.DAVE_EXECUTE_TRANSITION -> {}
                Opcodes.DAVE_TRANSITION_READY -> {}
                Opcodes.DAVE_PREPARE_EPOCH -> {}
                Opcodes.DAVE_MLS_EXTERNAL_SENDER -> {}
                Opcodes.DAVE_MLS_KEY_PACKAGE -> {}
                Opcodes.DAVE_MLS_PROPOSALS -> {}
                Opcodes.DAVE_MLS_COMMIT_WELCOME -> {}
                Opcodes.DAVE_MLS_ANNOUNCE_COMMIT_TRANSITION -> {}
                Opcodes.DAVE_MLS_WELCOME -> {}
                Opcodes.DAVE_MLS_INVALID_COMMIT_WELCOME -> {}
                Opcodes.CLIENT_FLAGS -> {}
                Opcodes.CLIENT_PLATFORM -> {}
                else -> {
                    handled = false
                }
            }
            if (handled) {
                param.result = Unit.a
            }
        }
    }
}


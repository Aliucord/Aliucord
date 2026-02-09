package com.aliucord.coreplugins.voice

import com.aliucord.utils.GsonUtils.fromJson
import com.discord.rtcconnection.socket.io.Opcodes
import com.discord.rtcconnection.socket.io.Payloads
import com.google.gson.Gson

sealed interface SunflowerPayload {
    sealed interface Incoming : SunflowerPayload
    sealed interface Outgoing : SunflowerPayload {
        val opcode: Int get() {
            return when (this) {
                is DaveTransitionReady -> Opcodes.DAVE_TRANSITION_READY
            }
        }
    }

    companion object {
        fun deserialize(gson: Gson, payload: Payloads.Incoming): Incoming? {
            val clazz = when (payload.opcode) {
                Opcodes.DAVE_PREPARE_TRANSITION -> DavePrepareTransition::class
                Opcodes.DAVE_EXECUTE_TRANSITION -> DaveExecuteTransition::class
                Opcodes.DAVE_PREPARE_EPOCH -> DavePrepareEpoch::class
                else -> null
            }
            return clazz?.java?.let { gson.fromJson(payload.data, it) }
        }
    }

    data class DavePrepareTransition(
        val protocolVersion: Int,
        val transitionId: Int,
    ) : Incoming

    data class DaveExecuteTransition(
        val transitionId: Int,
    ) : Incoming

    data class DavePrepareEpoch(
        val protocolVersion: Int,
        val epoch: Int,
    ) : Incoming

    data class DaveTransitionReady(
        val transitionId: Int,
    ) : Outgoing

    // data class DaveInvalidCommitWelcome(
    //     val transitionId: Int,
    // ) : Outgoing
}

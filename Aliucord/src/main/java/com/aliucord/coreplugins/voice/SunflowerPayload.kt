package com.aliucord.coreplugins.voice

import com.aliucord.utils.GsonUtils.fromJson
import com.aliucord.utils.SerializedName
import com.discord.rtcconnection.socket.io.Opcodes
import com.discord.rtcconnection.socket.io.Payloads
import com.google.gson.Gson

sealed interface SunflowerPayload {
    sealed interface Incoming : SunflowerPayload
    sealed interface Outgoing : SunflowerPayload {
        val opcode: Int get() {
            return when (this) {
                is DaveTransitionReady -> Opcodes.DAVE_TRANSITION_READY
                is DaveInvalidCommitWelcome -> Opcodes.DAVE_MLS_INVALID_COMMIT_WELCOME
            }
        }
    }

    companion object {
        fun deserialize(gson: Gson, payload: Payloads.Incoming): Incoming? {
            val clazz = when (payload.opcode) {
                Opcodes.CLIENTS_CONNECT -> ClientsConnect::class
                Opcodes.CLIENT_DISCONNECT -> ClientDisconnect::class
                Opcodes.DAVE_PREPARE_TRANSITION -> DavePrepareTransition::class
                Opcodes.DAVE_EXECUTE_TRANSITION -> DaveExecuteTransition::class
                Opcodes.DAVE_PREPARE_EPOCH -> DavePrepareEpoch::class
                else -> null
            }
            return clazz?.java?.let { gson.fromJson(payload.data, it) }
        }
    }

    data class ClientsConnect(
        @SerializedName("user_ids") val userIds: List<String>,
    ) : Incoming

    data class ClientDisconnect(
        @SerializedName("user_id") val userId: String,
    ) : Incoming

    data class DavePrepareTransition(
        @SerializedName("protocol_version") val protocolVersion: Int,
        @SerializedName("transition_id") val transitionId: Int,
    ) : Incoming

    data class DaveExecuteTransition(
        @SerializedName("transition_id") val transitionId: Int,
    ) : Incoming

    data class DavePrepareEpoch(
        @SerializedName("protocol_version") val protocolVersion: Int,
        val epoch: Int,
    ) : Incoming

    data class DaveTransitionReady(
        @SerializedName("transition_id") val transitionId: Int,
    ) : Outgoing

    data class DaveInvalidCommitWelcome(
        @SerializedName("transition_id") val transitionId: Int,
    ) : Outgoing
}

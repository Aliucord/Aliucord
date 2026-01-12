package com.aliucord.coreplugins.voice

sealed class Voice2Payloads {
    data class ClientsConnect(
        val userIds: List<Long>
    ) : Voice2Payloads()
}

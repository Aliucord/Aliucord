package com.discord.native.engine

data class ConnectionInfo @JvmOverloads constructor(
    val isConnected: Boolean,
    val protocol: String,
    val localAddress: String,
    val localPort: Int,
    val createConnectionTime: Int? = null,
    val connectTime: Int? = null,
)

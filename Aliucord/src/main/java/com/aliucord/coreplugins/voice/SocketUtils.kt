package com.aliucord.coreplugins.voice

import co.discord.media_engine.Connection
import com.aliucord.Logger
import com.discord.rtcconnection.RtcConnection
import com.discord.rtcconnection.socket.io.Opcodes
import okio.ByteString
import b.a.q.h0 as RtcConnectionEventHandler
import b.a.q.m0.c.e as MediaEngineConnectionLegacy
import b.a.q.n0.a as RtcControlSocket
import f0.e0.n.d as RealWebsocket

private val logger = Logger("VoiceChatFix")

val RtcControlSocket.rtcConnections: List<RtcConnection> get() {
    return this.q // this.eventHandlers
        .mapNotNull { eventHandler ->
            val eventHandler = eventHandler as? RtcConnectionEventHandler
            if (eventHandler == null) {
                logger.error("ConnectionEventHandler failed to cast", null)
                return@mapNotNull null
            }

            eventHandler.a
        }
        .distinct()
}

val RtcControlSocket.rtcConnection: RtcConnection? get() {
    val connections = rtcConnections

    if (connections.isEmpty()) {
        logger.warn("No rtcconnection found")
    } else if (connections.size > 1) {
        logger.warn("More than one rtcconnection found, using the first one")
    }
    return connections.getOrNull(0)
}

inline val RtcConnection.channelId get() = P
inline val RtcConnection.rtcServerId: String get() = S

val RtcConnection.groupId: Long get() {
    val streamKey: String? = d0
    val isScreenshare = streamKey != null
    return if (isScreenshare) {
        rtcServerId.toLong() - 1
    } else {
        channelId
    }
}

val RtcControlSocket.connections: List<Connection> get() {
    val connections = rtcConnections.map {
        // rtcConnection.mediaEngineConnection
        val engine = it.x as MediaEngineConnectionLegacy // Should never fail as there's only one impl
        // engine.connection
        engine.j
    }.distinct()

    if (connections.isEmpty()) {
        logger.warn("No connection found whilst handling event")
    } else if (connections.size > 1) {
        logger.warn("More than one connection found, passing event to all")
    }
    return connections
}

@Suppress("NOTHING_TO_INLINE")
inline fun RtcControlSocket.send(data: VoiceChatFixPayload.Outgoing) = n(data.opcode, data)

// Ref: https://github.com/square/okhttp/blob/c7556e0ac6d690ccb71d304d22d636f2f86baf7b/okhttp/src/commonJvmAndroid/kotlin/okhttp3/internal/ws/RealWebSocket.kt#L434-L456
// Also: RealWebsocket.a(String)
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

fun RtcControlSocket.send(opcode: Int, bytes: ByteString) {
    // Prepend the opcode as the first byte before the data
    val data = bytes.prepend(opcode.toByte())
    logger.debug("Sending opcode ${Opcodes.friendly(opcode)}: ${data.encodeBase64()} ")
    send(data)
}

package com.aliucord.coreplugins.voice

import com.aliucord.Logger
import com.aliucord.coreplugins.voice.model.ChannelInfoRequest
import com.discord.gateway.GatewaySocket
import com.discord.gateway.io.Outgoing
import com.discord.stores.StoreStream
import java.util.Collections
import java.util.Locale
import b.a.q.n0.a as RtcControlSocket

internal object VoiceChatTimers {
    private val logger = Logger("VoiceChatTimers")
    internal val callStartTimes = Collections.synchronizedMap(HashMap<Long, Long>())

    @Volatile
    internal var gatewaySocket: GatewaySocket? = null

    // Gateway send opcode, requests a CHANNEL_INFO dispatch with the
    // ephemeral voice fields (status, voice_start_time) for a guild
    internal const val OPCODE_REQUEST_CHANNEL_INFO = 43

    internal fun backstopTimeSelected(currentSocket: RtcControlSocket?, current: Long?): Long? {
        if (current != 0L) return current

        val channelId = StoreStream.getVoiceChannelSelected().selectedVoiceChannelId
            .takeIf { it > 0L }
            ?: currentSocket?.rtcConnection?.channelId
            ?: return current

        return callStartTimes.getOrPut(channelId) { System.currentTimeMillis() }
    }

    // Tracks the start time for every channel so the duration backstop below
    // works even when the event precedes selection; null clears the entry
    internal fun trackCallStart(channelId: Long, startTimeSec: Long?) {
        if (startTimeSec == null) callStartTimes.remove(channelId)
        else callStartTimes[channelId] = startTimeSec * 1000
    }

    // Asks the gateway for the ephemeral voice fields of a guild's channels,
    // answered with a CHANNEL_INFO dispatch
    // TEST: docs say this op is guild-only; sending anyway with guildId=0 for DM/GDM
    // channels to see what the gateway actually does (ignore/empty reply/close code)
    internal fun requestChannelInfo(guildId: Long) {
        if (guildId == 0L) logger.warn("requestChannelInfo(0): guild-only per docs, sending anyway as test")

        val socket = gatewaySocket ?: run {
            logger.warn("requestChannelInfo($guildId): no gateway socket captured yet")
            return
        }

        logger.debug("Requesting channel info for guild $guildId")

        GatewaySocket.`send$default`(
            socket,
            Outgoing(OPCODE_REQUEST_CHANNEL_INFO, ChannelInfoRequest(guildId, listOf("status", "voice_start_time"))),
            false, null, 6, null,
        )
    }


    internal fun callTimersLines(start: Long): List<String> {
        val now = System.currentTimeMillis()
        val secs = ((now - start) / 1000).coerceAtLeast(0)

        val (days, hours, minutes, seconds) = listOf(secs / 86400, secs / 3600 % 24, secs / 60 % 60, secs % 60)
        val mmss = String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)

        val elapsed = when {
            days > 0 -> "$days:${"%02d".format(hours)}:$mmss"
            hours > 0 -> "$hours:$mmss"
            else -> mmss
        }

        return listOf(
            elapsed,
            // DateFormat.getTimeInstance().format(Date(start)),
            // DateFormat.getDateInstance().format(Date(start)),
            // DateUtils.getRelativeTimeSpanString(start, now, DateUtils.SECOND_IN_MILLIS),
        )
    }
}

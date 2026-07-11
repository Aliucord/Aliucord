package com.aliucord.coreplugins.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.api.GatewayAPI
import com.aliucord.api.PatcherAPI
import com.aliucord.coreplugins.voice.model.VoiceChannelEffect
import com.aliucord.coreplugins.voice.ui.isSoundboardMuted
import com.aliucord.patcher.before
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.aliucord.patcher.component3
import com.discord.gateway.GatewaySocket
import com.discord.stores.StoreStream
import java.io.File
import java.net.URL
import java.util.Collections

internal object Soundboard {
    private val logger = Logger("VoiceChatFix")
    private const val EVENT_NAME = "VOICE_CHANNEL_EFFECT_SEND"
    private const val MAX_CACHE_BYTES = 16L * 1024 * 1024  // 16 MB
    private const val MAX_PLAYERS = 8
    private lateinit var cacheDir: File
    private val activePlayers = Collections.synchronizedSet(mutableSetOf<MediaPlayer>())

    fun register(patcher: PatcherAPI, context: Context) {
        cacheDir = File(context.cacheDir, "soundboard").apply { mkdirs() }
        GatewayAPI.onEvent<VoiceChannelEffect>(EVENT_NAME) { play(it) }

        patcher.before<GatewaySocket>(
            "handleDispatch",
            Object::class.java,
            String::class.javaObjectType,
            Int::class.javaPrimitiveType!!,
            Int::class.javaPrimitiveType!!,
            Long::class.javaPrimitiveType!!,
        ) { (param, _: Any, event: String) ->
            if (event == EVENT_NAME) param.args[0] = Unit.a
        }
    }

    private fun play(effect: VoiceChannelEffect) {
        val soundId = effect.soundId ?: return
        if (StoreStream.getVoiceChannelSelected().selectedVoiceChannelId != effect.channelId) return
        val config = StoreStream.getMediaSettings().voiceConfigurationBlocking
        if (config.isSelfDeafened) return

        // Muting a user also mutes their soundboard
        if (config.mutedUsers[effect.userId] == true || isSoundboardMuted(effect.userId)) {
            logger.debug("Skipping soundboard sound $soundId from muted user ${effect.userId}")
            return
        }
        logger.debug("Playing soundboard sound $soundId from user ${effect.userId}")

        // Scale sound volume depending on the sender's local volume
        val volume = (effect.soundVolume ?: 1f).coerceIn(0f, 1f) *
            (VoiceChatFixSettings.soundboardVolume.coerceIn(0, 100) / 100f)

        if (volume == 0f) {
            logger.debug("Skipping soundboard sound $soundId since volume is 0")
            return
        }

        // Soundboard is in .ogg, which MediaPlayer cannot play, skill issue from NuPlayer
        // So we download and cache it which is genius since SOME spam soundboard
        Utils.threadPool.execute {
            runCatching {
                val file = fetchSound(soundId)
                clearOldestPlayers()

                MediaPlayer().apply {
                    activePlayers.add(this)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(file.absolutePath)
                    setVolume(volume, volume)
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener {
                        activePlayers.remove(it)
                        it.release()
                    }
                    setOnErrorListener { player, what, extra ->
                        logger.warn("Soundboard playback error $what/$extra (sound $soundId)")
                        activePlayers.remove(player)
                        player.release()
                        true
                    }
                    prepareAsync()
                }
            }.onFailure { logger.error("Failed to play soundboard sound $soundId", it) }
        }
    }

    private fun fetchSound(soundId: String): File {
        logger.debug("Fetching soundboard sound $soundId")
        val file = File(cacheDir, soundId)

        if (file.exists() && file.length() > 0L) {
            logger.debug("Soundboard sound $soundId already exists in cache, using it")
            file.setLastModified(System.currentTimeMillis())
            return file
        }

        val tmp = File(cacheDir, "$soundId.tmp")
        try {
            URL("https://cdn.discordapp.com/soundboard-sounds/$soundId").openStream().use { input ->
                tmp.outputStream().use { input.copyTo(it) }
            }
            tmp.renameTo(file)
        } finally {
            tmp.delete()
        }

        clearCache()
        return file
    }

    private fun clearCache() {
        val files = cacheDir.listFiles() ?: return
        var total = files.sumOf { it.length() }
        if (total <= MAX_CACHE_BYTES) return

        logger.debug("Starting clearing cache of soundboard sounds... fileNum=${files.size} sizeTotal=$total")

        for (f in files.sortedBy { it.lastModified() }) {
            if (total <= MAX_CACHE_BYTES) break
            val len = f.length()
            if (f.delete()) {
                total -= len
                logger.debug("Deleted cached soundboard sound '${f.name}'")
            }
        }
    }

    private fun clearOldestPlayers() {
        val players = mutableListOf<MediaPlayer>()

        synchronized(activePlayers) {
            val iterator = activePlayers.iterator()
            while (activePlayers.size >= MAX_PLAYERS && iterator.hasNext()) {
                players.add(iterator.next())
                iterator.remove()
            }
        }

        players.forEach { oldest ->
            runCatching { oldest.stop() }
                .onFailure { logger.debug("Player $oldest not started yet, releasing directly") }
            runCatching { oldest.release() }
                .onFailure { logger.error("Failed to release player: $oldest", it) }
                .onSuccess { logger.debug("Released player: $oldest") }
        }
    }
}

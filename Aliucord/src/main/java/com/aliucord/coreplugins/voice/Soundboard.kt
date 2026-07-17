package com.aliucord.coreplugins.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.api.GatewayAPI
import com.aliucord.coreplugins.voice.model.SoundboardSound
import com.aliucord.coreplugins.voice.model.VoiceChannelEffect
import com.aliucord.coreplugins.voice.ui.isSoundboardMuted
import com.aliucord.utils.RxUtils.subscribe
import com.aliucord.wrappers.ChannelWrapper.Companion.guildId
import com.aliucord.wrappers.users.globalName
import com.discord.stores.StoreStream
import java.io.File
import java.net.URL
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

internal object Soundboard {
    private val logger = Logger("VoiceChatFix")
    private const val MAX_CACHE_BYTES = 16L * 1024 * 1024  // 16 MB
    private const val MAX_PLAYERS = 8
    private lateinit var cacheDir: File
    private val activePlayers = Collections.synchronizedSet(mutableSetOf<MediaPlayer>())
    private var lastChannelId: Long? = null
    private val soundNames = ConcurrentHashMap<String, String>()

    fun register(context: Context) {
        cacheDir = File(context.cacheDir, "soundboard").apply { mkdirs() }

        GatewayAPI.onEvent<VoiceChannelEffect>("VOICE_CHANNEL_EFFECT_SEND") { handle(it) }
        GatewayAPI.onEvent<SoundboardSound>("GUILD_SOUNDBOARD_SOUND_CREATE") { cacheName(it) }
        GatewayAPI.onEvent<SoundboardSound>("GUILD_SOUNDBOARD_SOUND_UPDATE") { cacheName(it) }
        GatewayAPI.onEvent<SoundboardSound>("GUILD_SOUNDBOARD_SOUND_DELETE") { it.soundId?.let(soundNames::remove) }

        // stop playing sounds if user leaves vc or deafens
        StoreStream.getVoiceChannelSelected().observeSelectedVoiceChannelId().subscribe {
            if (this != lastChannelId) {
                lastChannelId = this
                stopAllPlayers("voice channel changed")
            }
        }

        StoreStream.getMediaSettings().isSelfDeafened().subscribe {
            if (this) stopAllPlayers("self-deafened")
        }
    }

    private fun cacheName(sound: SoundboardSound) {
        val id = sound.soundId ?: return
        val name = sound.name ?: return
        soundNames[id] = name
    }

    private fun handle(effect: VoiceChannelEffect) {
        val soundId = effect.soundId
        if (StoreStream.getVoiceChannelSelected().selectedVoiceChannelId != effect.channelId) return
        val config = StoreStream.getMediaSettings().voiceConfigurationBlocking
        if (config.isSelfDeafened || isServerDeafened(effect.channelId)) return

        // Muting a user also mutes their soundboard and effect notifications
        if (config.mutedUsers[effect.userId] == true || isSoundboardMuted(effect.userId)) {
            logger.debug("Skipping voice effect from muted user ${effect.userId} soundId=$soundId")
            return
        }

        notify(effect)
        effect.soundId?.let { play(effect, it, config.userOutputVolumes[effect.userId]) }
    }

    private fun isServerDeafened(channelId: Long): Boolean {
        val guildId = StoreStream.getChannels().getChannel(channelId)?.guildId ?: return false
        if (guildId == 0L) return false

        val meId = StoreStream.getUsers().me.id
        val state = StoreStream.getVoiceStates().getForChannel(guildId, channelId)
            ?.get(meId) ?: return false

        // `b` = serverDeaf ; `g` = selfDeaf
        return state.b() || state.g()
    }

    // Nothing is shown when someone sends a voice channel effect
    private fun notify(effect: VoiceChannelEffect) {
        if (!VoiceChatFixSettings.effectNotifications) return
        if (effect.userId == StoreStream.getUsers().me.id) return

        val user = StoreStream.getUsers().users[effect.userId]
        val username = user?.username ?: user?.globalName ?: "unknown user"

        // Unicode emoji come with id=null and the character in name; custom emoji only
        // have a name we can show as :name:.
        val emoji = effect.emoji?.let { if (it.id == null) it.name else it.name?.let { n -> ":$n:" } }

        val text = when {
            effect.soundId != null -> {
                val name = soundNames[effect.soundId]
                val played = if (name.isNullOrEmpty()) "played a sound" else "played \"$name\""

                "$username $played${emoji?.let { " $it" } ?: ""}"
            }
            emoji != null -> "$username sent $emoji"
            else -> return
        }

        logger.debug("Showing effect sound toast from user ${effect.userId} effect=$effect")
        Utils.mainThread.post { Utils.showToast(text) }
    }

    private fun play(effect: VoiceChannelEffect, soundId: String, senderVolume: Float?) {
        logger.debug("Playing soundboard sound $soundId from user ${effect.userId}")

        // per-user sound volume * global soundboard volume * per-user volume
        val volume = ((effect.soundVolume ?: 1f).coerceIn(0f, 1f) *
            (VoiceChatFixSettings.soundboardVolume.coerceIn(0, 100) / 100f) *
            ((senderVolume ?: 100f) / 100f)).coerceIn(0f, 1f)

        if (volume == 0f) {
            logger.debug("Skipping soundboard sound $soundId since volume is 0")
            return
        }

        // Soundboard is in .ogg, which MediaPlayer cannot play, skill issue from NuPlayer
        // So we download and cache it which is genius since SOME spam soundboard
        Utils.threadPool.execute {
            val file = try {
                fetchSound(soundId)
            } catch (e: Throwable) {
                logger.error("Failed to fetch soundboard sound $soundId", e)
                return@execute
            }

            runCatching {
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
                    setOnPreparedListener { player ->
                        runCatching {
                            if (activePlayers.contains(player)) player.start()
                        }.onFailure {
                            logger.debug("Player destroyed before sound could start (sound $soundId)")
                        }
                    }
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

        players.forEach(::releasePlayer)
    }

    private fun stopAllPlayers(reason: String?) {
        val players: List<MediaPlayer>
        val reason = if (!reason.isNullOrEmpty()) ": $reason" else ""

        synchronized(activePlayers) {
            if (activePlayers.isEmpty()) return

            players = activePlayers.toList()
            activePlayers.clear()
        }

        logger.debug("Stopping ${players.size} soundboard player(s)$reason")
        players.forEach(::releasePlayer)
    }

    private fun releasePlayer(player: MediaPlayer) {
        runCatching { player.stop() }
            .onFailure { logger.debug("Player $player not started yet, releasing directly") }
        runCatching { player.release() }
            .onFailure { logger.error("Failed to release player: $player", it) }
            .onSuccess { logger.debug("Released player: $player") }
    }
}

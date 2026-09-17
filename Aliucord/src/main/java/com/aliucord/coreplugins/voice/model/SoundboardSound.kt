package com.aliucord.coreplugins.voice.model

import com.aliucord.utils.SerializedName

// /guilds/{id}/soundboard-sounds + /soundboard-default-sounds
// Gateway Payloads:
// GUILD_SOUNDBOARD_SOUND_CREATE
// GUILD_SOUNDBOARD_SOUND_UPDATE
// GUILD_SOUNDBOARD_SOUND_DELETE - only carries sound_id + guild_id
internal data class SoundboardSound(
    @SerializedName("sound_id") val soundId: String?,
    val name: String?,
)

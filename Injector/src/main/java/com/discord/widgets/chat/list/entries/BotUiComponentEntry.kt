package com.discord.widgets.chat.list.entries

import com.discord.api.channel.Channel
import com.discord.api.role.GuildRole
import com.discord.models.botuikit.MessageComponent
import com.discord.models.member.GuildMember
import com.discord.models.message.Message
import com.discord.stores.StoreMessageState

// Replacement for Discord's stock BotUiComponentEntry with extra fields
// .copy isn't called anywhere, so we don't have to worry about creating its overload
data class BotUiComponentEntry(
    // Original fields
    val message: Message,
    val applicationId: Long,
    val guildId: Long?,
    val messageComponents: List<MessageComponent>,

    // New fields
    val state: StoreMessageState.State?,
    val meId: Long,
    val channel: Channel,
    val guildMembers: Map<Long, GuildMember>,
    val guildRoles: Map<Long, GuildRole>,
    // val channelNames: Map<Long, String>,
): ChatListEntry() {
    override fun getKey() = "$type -- ${message.nonce ?: message.id}"
    override fun getType() = 36

    /**
     * Retain function signature of old constructor
     * It will be properly recreated with a patch in ComponentsV2.patchMessageItems
     */
    @Suppress("null_for_nonnull_type", "unused")
    internal constructor(
        message: Message,
        applicationId: Long,
        guildId: Long?,
        messageComponents: List<MessageComponent>,
    ): this(
        message, applicationId, guildId, messageComponents,
        null,
        0,
        null,
        null,
        null,
    )
}

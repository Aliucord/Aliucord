/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.entities

import android.content.Context
import com.aliucord.wrappers.ChannelWrapper
import com.aliucord.wrappers.GuildRoleWrapper
import com.discord.api.message.LocalAttachment
import com.discord.api.message.MessageReference
import com.discord.models.member.GuildMember
import com.discord.models.message.Message
import com.discord.models.user.MeUser
import com.discord.models.user.User
import com.discord.stores.StoreStream
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.attachments.AttachmentUtilsKt
import com.discord.widgets.chat.MessageContent
import com.discord.widgets.chat.input.*
import com.discord.widgets.chat.input.ChatInputViewModel.ViewState.Loaded.PendingReplyState
import com.lytefast.flexinput.model.Attachment
import java.util.*

/** Context passed to command executors  */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class CommandContext(
    /** Returns the raw args  */
    private val rawArgs: Map<String, *>,
    private val _this: `WidgetChatInput$configureSendListeners$2`,
    args: Array<Any>,
    private val messageContent: MessageContent
) {
    /**
     * Looks like Discord's required args are unreliable and are sometimes accepted even if empty.
     * Or a plugin dev may just forget to mark an argument as required which leads to ugly NPEs.
     * Thus, throw a custom exception and handle it in the command handler to present the user a simple message, not a scary
     * stacktrace.
     */
    class RequiredArgumentWasNullException(name: String?) : RuntimeException(
        "Required argument $name was null. Please specify a value for it and try again.",
    )

    companion object {
        private fun <T> requireNonNull(key: String, `val`: T?): T {
            if (`val` != null) return `val`
            throw RequiredArgumentWasNullException(key)
        }
    }

    /** Returns the ViewState associated with this Context  */
    val viewState: ChatInputViewModel.ViewState.Loaded = (args[2] as `WidgetChatInput$configureSendListeners$7$1`).`this$0`.`$viewState`
    var attachments: MutableList<Attachment<*>>
        private set

    init {
        attachments = (args[0] as List<Attachment<*>>).toMutableList()
    }

    /** Returns the AppContext  */
    val context: Context
        get() = _this.`$context`

    /** Returns the maximum size attachments may be  */
    val maxFileSizeMB: Int
        get() = viewState.maxFileSizeMB

    val replyingState: PendingReplyState.Replying?
        get() {
            val state: PendingReplyState = viewState.pendingReplyState
            return if (state is PendingReplyState.Replying) state else null
        }

    /** Returns the MessageReference  */
    val messageReference: MessageReference?
        get() = replyingState?.messageReference

    /** Returns the Author of the referenced message  */
    val referencedMessageAuthor: User?
        get() = replyingState?.repliedAuthor

    /** Returns the Author of the referenced message as member of the current guild  */
    val referencedMessageAuthorGuildMember: GuildMember?
        get() = replyingState?.repliedAuthorGuildMember

    /** Returns the referenced message  */
    val referencedMessage: Message?
        get() {
            val ref = messageReference ?: return null
            return StoreStream.getMessages().getMessage(ref.a(), ref.c())
        }

    /** Returns the link of the referenced message  */
    val referencedMessageLink: String?
        get() {
            val ref = messageReference ?: return null
            val guildId = if (ref.b() != null) ref.b().toString() else "@me"
            return "https://discord.com/channels/$guildId/${ref.a()}/${ref.c()}"
        }

    /** Returns the current channel id  */
    /** Sets the current channel id  */
    var channelId: Long
        get() = _this.`$chatInput`.channelId
        set(id) {
            _this.`$chatInput`.channelId = id
        }

    /**
     * Returns the current channel
     */
    @get:Deprecated("This method is deprecated in favor of getCurrentChannel()", ReplaceWith("currentChannel"))
    val channel: ChannelWrapper
        get() = currentChannel

    /** Returns the current channel  */
    val currentChannel: ChannelWrapper
        get() = ChannelWrapper(viewState.channel)

    /** Returns the raw content of the message that invoked this command  */
    val rawContent: String
        get() = messageContent.textContent

    /**
     * Adds an attachment
     * @param uri Uri of the attachment
     * @param displayName file name
     */
    fun addAttachment(uri: String?, displayName: String?) {
        addAttachment(
            LocalAttachment(
                SnowflakeUtils.fromTimestamp(System.currentTimeMillis()),
                uri,
                displayName
            )
        )
    }

    /**
     * Adds an attachment
     * @param attachment Attachment
     */
    fun addAttachment(attachment: LocalAttachment) {
        addAttachment(AttachmentUtilsKt.toAttachment(attachment))
    }

    /**
     * Adds an attachment
     * @param attachment Attachment
     */
    fun addAttachment(attachment: Attachment<*>) {
        if (attachments !is ArrayList<*>)
            attachments = ArrayList<Attachment<*>>(attachments)
        attachments.add(attachment)
    }

    /** Returns the mentioned users  */
    val mentionedUsers: List<User>
        get() = messageContent.mentionedUsers

    /** Returns the current user  */
    val me: MeUser
        get() = StoreStream.getUsers().me

    /**
     * Check if the arguments contain the specified key
     * @param key Key to check
     */
    fun containsArg(key: String) = rawArgs.containsKey(key)

    /**
     * Gets the arguments object for the specified subcommand
     * @param key Key of the subcommand
     */
    @Suppress("UNCHECKED_CAST")
    fun getRequiredSubCommandArgs(key: String) = requireNonNull(key, rawArgs[key]) as Map<String, *>

    /**
     * Gets the arguments object for the specified subcommand
     * @param key Key of the subcommand
     */
    @Suppress("UNCHECKED_CAST")
    fun getSubCommandArgs(key: String) = rawArgs[key] as Map<String, *>?

    /**
     * Gets the raw argument with the specified key
     * @param key The key of the argument
     */
    operator fun get(key: String): Any? = rawArgs[key]

    /**
     * Gets the **required** raw argument with the specified key
     * @param key The key of the argument
     */
    fun getRequired(key: String) = requireNonNull(key, get(key))

    /**
     * Gets the raw argument with the specified key or the defaultValue if no such argument is present
     * @param key The key of the argument
     * @param defaultValue The default value
     */
    fun getOrDefault(key: String, defaultValue: Any) = get(key) ?: defaultValue

    /**
     * Gets the String argument with the specified key
     * @param key The key of the argument
     */
    fun getString(key: String) = rawArgs[key] as String?

    /**
     * Gets the **required** String argument with the specified key
     * @param key The key of the argument
     */
    fun getRequiredString(key: String) = requireNonNull(key, getString(key))

    /**
     * Gets the String argument with the specified key or the defaultValue if no such argument is present
     * @param key The key of the argument
     */
    fun getStringOrDefault(key: String, defaultValue: String) = getString(key) ?: defaultValue

    /**
     * Gets the Integer argument with the specified key
     * @param key The key of the argument
     */
    fun getInt(key: String) = when (val v = get(key)) {
        null -> null
        is Int -> v
        is Long -> v.toInt()
        is String -> Integer.valueOf(v)
        else -> throw ClassCastException(
            "Argument $key is of type ${v.javaClass.simpleName} which cannot be cast to Integer.",
        )
    }

    /**
     * Gets the **required** Integer argument with the specified key
     * @param key The key of the argument
     */
    fun getRequiredInt(key: String) = requireNonNull(key, getInt(key))

    /**
     * Gets the Integer argument with the specified key or the defaultValue if no such argument is present
     * @param key The key of the argument
     */
    fun getIntOrDefault(key: String, defaultValue: Int) = getInt(key) ?: defaultValue

    /**
     * Gets the Long argument with the specified key
     * @param key The key of the argument
     */
    fun getLong(key: String) = when (val v = get(key)) {
        null -> null
        is Long -> v
        is Int -> v.toLong()
        is String -> java.lang.Long.valueOf(v)
        else -> throw ClassCastException(
            "Argument $key is of type ${v.javaClass.simpleName} which cannot be cast to Integer.",
        )
    }

    /**
     * Gets the **required** Long argument with the specified key
     * @param key The key of the argument
     */
    fun getRequiredLong(key: String) = requireNonNull(key, getLong(key))

    /**
     * Gets the Long argument with the specified key or the defaultValue if no such argument is present
     * @param key The key of the argument
     */
    fun getLongOrDefault(key: String, defaultValue: Long) = getLong(key) ?: defaultValue

    /**
     * Gets the Boolean argument with the specified key
     * @param key The key of the argument
     */
    fun getBool(key: String) = when (val v = get(key)) {
        null -> null
        is Boolean -> v
        is String -> java.lang.Boolean.valueOf(v)
        else -> throw ClassCastException(
            "Argument $key is of type ${v.javaClass.simpleName} which cannot be cast to Integer.",
        )
    }

    /**
     * Gets the **required** Boolean argument with the specified key
     * @param key The key of the argument
     */
    fun getRequiredBool(key: String) = requireNonNull(key, getBool(key))

    /**
     * Gets the Boolean argument with the specified key or the defaultValue if no such argument is present
     * @param key The key of the argument
     */
    fun getBoolOrDefault(key: String, defaultValue: Boolean) = getBool(key) ?: defaultValue

    /**
     * Gets the User argument with the specified key
     * @param key The key of the argument
     */
    fun getUser(key: String): User? {
        return StoreStream.getUsers().users[getLong(key) ?: return null]
    }

    /**
     * Gets the **required** User argument with the specified key
     * @param key The key of the argument
     */
    fun getRequiredUser(key: String) = requireNonNull(key, getUser(key))

    /**
     * Gets the User argument with the specified key or the defaultValue if no such argument is present
     * @param key The key of the argument
     */
    fun getUserOrDefault(key: String, defaultValue: User) = getUser(key) ?: defaultValue

    /**
     * Gets the Channel argument with the specified key
     * @param key Key of the argument
     */
    fun getChannel(key: String): ChannelWrapper? {
        return ChannelWrapper(StoreStream.getChannels().getChannel(getLong(key) ?: return null))
    }

    /**
     * Gets the **required** channel argument with the specified key
     * @param key The key of the argument
     */
    fun getRequiredChannel(key: String) = requireNonNull(key, getChannel(key))

    /**
     * Gets the channel argument with the specified key or the defaultValue if no such argument is present
     * @param key The key of the argument
     */
    fun getChannelOrDefault(key: String, defaultValue: ChannelWrapper) = getChannel(key) ?: defaultValue

    /**
     * Gets the Role argument with the specified key
     * @param key Key of the argument
     */
    fun getRole(key: String): GuildRoleWrapper? {
        val id = getLong(key)
        val roles = StoreStream.getGuilds().roles[currentChannel.guildId]
        if (id == null || roles == null) return null
        return GuildRoleWrapper(roles[id] ?: return null)
    }

    /**
     * Gets the **required** Role argument with the specified key
     * @param key Key of the argument
     */
    fun getRequiredRole(key: String) = requireNonNull(key, getRole(key))

    /**
     * Gets the Role argument with the specified key or the defaultValue if no such argument is present
     * @param key The key of the argument
     */
    fun getRoleOrDefault(key: String, defaultValue: GuildRoleWrapper) = getRole(key) ?: defaultValue
}

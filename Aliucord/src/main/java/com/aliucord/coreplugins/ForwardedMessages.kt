package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.aliucord.coreplugins.forwardedmessages.ForwardSourceChatEntry
import com.aliucord.coreplugins.forwardedmessages.WidgetChatListAdapterItemForwardSource
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.Hook
import com.aliucord.patcher.PreHook
import com.aliucord.updater.ManagerBuild.hasInjector
import com.aliucord.updater.ManagerBuild.hasPatches
import com.aliucord.utils.ReflectUtils
import com.discord.api.application.Application
import com.discord.api.channel.Channel
import com.discord.api.interaction.Interaction
import com.discord.api.message.Message
import com.discord.api.message.MessageReference
import com.discord.api.message.MessageSnapshot
import com.discord.api.message.activity.MessageActivity
import com.discord.api.message.allowedmentions.MessageAllowedMentions
import com.discord.api.message.call.MessageCall
import com.discord.api.message.role_subscription.RoleSubscriptionData
import com.discord.api.user.User
import com.discord.api.utcdatetime.UtcDateTime
import com.discord.stores.StoreMessageState
import com.discord.stores.StoreStream
import com.discord.stores.StoreThreadMessages
import com.discord.utilities.captcha.CaptchaHelper.CaptchaPayload
import com.discord.utilities.embed.InviteEmbedModel
import com.discord.utilities.permissions.PermissionUtils
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.ChatListEntry
import com.discord.widgets.chat.list.entries.ReactionsEntry
import com.discord.widgets.chat.list.model.WidgetChatListModelMessages
import java.lang.reflect.Field
import java.util.List
import java.util.Map

@Suppress("UNCHECKED_CAST")
internal class ForwardedMessages : CorePlugin(Manifest("ForwardedMessages")) {
    override val isRequired = true
    override val isHidden = true

    private lateinit var apiMessageMessageSnapshotsField: Field
    private lateinit var modelMessageMessageSnapshotsField: Field

    private fun writeSnapshotFields(source: Any?, destination: Any, isApiMessage: Boolean): Any {
        // we only ever call this method on objects that have this field
        val snapshots = (if (isApiMessage) apiMessageMessageSnapshotsField else modelMessageMessageSnapshotsField)[source] as ArrayList<MessageSnapshot?>?

        if (snapshots == null || snapshots.isEmpty()) return destination

        val messageSnapshot = checkNotNull(snapshots[0]!!.message)
        ReflectUtils.setField(destination, "messageSnapshots", snapshots)
        ReflectUtils.setField(destination, "embeds", messageSnapshot.k())
        ReflectUtils.setField(destination, "content", messageSnapshot.i())
        ReflectUtils.setField(destination, "attachments", messageSnapshot.d())
        ReflectUtils.setField(destination, "stickerItems", messageSnapshot.A())
        return destination
    }

    override fun start(context: Context) {
        if (!hasInjector("2.1.0") || !hasPatches("1.1.0")) {
            logger.warn("Base app outdated, cannot enable ForwardedMessages")
            return
        }

        // Cache reflection since this is used in a performance-sensitive areas
        apiMessageMessageSnapshotsField = Message::class.java.getDeclaredField("messageSnapshots")
        modelMessageMessageSnapshotsField = com.discord.models.message.Message::class.java.getDeclaredField("messageSnapshots")

        // Overrides message content if the message is actually a forward
        patcher.patch(
            com.discord.models.message.Message::class.java.getDeclaredConstructor(Message::class.java),
            Hook { param ->
                writeSnapshotFields(param.args[0], param.thisObject, true)
            }
        )

        // Keeps forward information when the message is updated (i.e. reacting)
        patcher.patch(
            com.discord.models.message.Message::class.java.getDeclaredMethod(
                "copy",
                Long::class.javaPrimitiveType,
                Long::class.javaPrimitiveType,
                Long::class.javaObjectType,
                User::class.java,
                String::class.java,
                UtcDateTime::class.java,
                UtcDateTime::class.java,
                Boolean::class.javaObjectType,
                Boolean::class.javaObjectType,
                List::class.java,
                List::class.java,
                List::class.java,
                List::class.java,
                List::class.java,
                String::class.java,
                Boolean::class.javaObjectType,
                Long::class.javaObjectType,
                Int::class.javaObjectType,
                MessageActivity::class.java,
                Application::class.java,
                Long::class.javaObjectType,
                MessageReference::class.java,
                Long::class.javaObjectType,
                List::class.java,
                List::class.java,
                Message::class.java,
                Interaction::class.java,
                Channel::class.java,
                List::class.java,
                MessageCall::class.java,
                Boolean::class.javaObjectType,
                RoleSubscriptionData::class.java,
                Boolean::class.javaPrimitiveType,
                MessageAllowedMentions::class.java,
                Int::class.javaObjectType,
                Long::class.javaObjectType,
                Long::class.javaObjectType,
                List::class.java,
                CaptchaPayload::class.java
            ), Hook { param ->
                param.result = writeSnapshotFields(param.thisObject, param.result, false)
            }
        )

        // Sets the bot tag to a FORWARDED tag, as it's the most convenient indication method
        patcher.patch(
            WidgetChatListAdapterItemMessage::class.java.getDeclaredMethod(
                "configureItemTag",
                com.discord.models.message.Message::class.java,
                Boolean::class.javaPrimitiveType
            ), PreHook  { param ->
                val snapshots = modelMessageMessageSnapshotsField[param.args[0]] as ArrayList<MessageSnapshot?>?
                if (snapshots.isNullOrEmpty()) return@PreHook

                val tw = ReflectUtils.getField(param.thisObject, "itemTag") as TextView? ?: return@PreHook
                tw.apply {
                    visibility = View.VISIBLE
                    text = "FORWARDED"
                    setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                    ) // Disables the verified checkmark, only done because RecyclerView
                }

                param.result = null
            }
        )

        // Resolve our custom chat item so that it can actually be displayed
        patcher.patch(
            WidgetChatListAdapter::class.java.getDeclaredMethod(
                "onCreateViewHolder",
                ViewGroup::class.java,
                Int::class.javaPrimitiveType
            ),
            PreHook { param ->  // I did a PreHook to avoid looking up existing types, its not really necessary but idc :) - Wing (wingio)
                val entryType = param.args[1] as Int
                if (entryType == ForwardSourceChatEntry.FORWARD_SOURCE_ENTRY_TYPE) {
                    param.result = WidgetChatListAdapterItemForwardSource(param.thisObject as WidgetChatListAdapter)
                }
            }
        )

        // Add a custom ChatListEntry for forwarded message sources
        // Yes, the function signature is actually that long, Discord devs were insane - Wing (wingio)
        patcher.patch(
            WidgetChatListModelMessages.Companion::class.java.getDeclaredMethod(
                "getMessageItems",
                Channel::class.java,
                Map::class.java,
                Map::class.java,
                Map::class.java,
                Channel::class.java,
                StoreThreadMessages.ThreadState::class.java,
                com.discord.models.message.Message::class.java,
                StoreMessageState.State::class.java,
                Map::class.java,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Long::class.javaObjectType,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Long::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                Map::class.java,
                InviteEmbedModel::class.java,
                Boolean::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType
            ),
            Hook { param ->
                val items = param.result as ArrayList<ChatListEntry?> // The return type for this method is List<T> but internally its an ArrayList<T>
                val msg = param.args[6] as com.discord.models.message.Message
                val snapshots = modelMessageMessageSnapshotsField[msg] as ArrayList<MessageSnapshot?>?
                val reference = msg.messageReference ?: return@Hook

                val originalChannel = StoreStream.getChannels().getChannel(reference.a()) ?: return@Hook // This also implicitly checks if the user is in the source guild

                // Checks if the current user has permission to access the source channel.
                // This is really only done if the user is in the source guild but
                // unable to access the source channel.
                if (!PermissionUtils.INSTANCE.hasAccess(
                        originalChannel,
                        StoreStream.getPermissions().permissionsByChannel[reference.a()]
                    )
                ) return@Hook

                if (snapshots != null && !snapshots.isEmpty()) { // Adds the source right before the list of reactions, if present
                    val reactionsEntry = items.stream().filter { chatListEntry -> chatListEntry is ReactionsEntry }
                        .findFirst()
                    val reactionsIdx = items.indexOf(reactionsEntry.orElse(null))
                    items.add(if (reactionsIdx != -1) reactionsIdx else items.size, ForwardSourceChatEntry(reference, msg.id))
                }

                param.result = items
            }
        )
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}

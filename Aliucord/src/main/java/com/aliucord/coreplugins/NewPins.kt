package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.SerializedName
import com.aliucord.utils.ViewUtils.addTo
import com.discord.models.message.Message
import com.discord.models.user.MeUser
import com.discord.stores.*
import com.discord.utilities.permissions.ManageMessageContext
import com.discord.utilities.permissions.PermissionUtils
import com.discord.utilities.rest.RestAPI
import com.discord.views.TernaryCheckBox
import com.discord.widgets.channels.permissions.WidgetChannelSettingsEditPermissions
import com.discord.widgets.channels.permissions.`WidgetChannelSettingsEditPermissions$permissionCheckboxes$2`
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.entries.LoadingEntry
import com.discord.widgets.chat.pins.WidgetChannelPinnedMessages
import rx.Observable
import java.net.URLEncoder
import java.util.WeakHashMap

import com.discord.api.message.Message as ApiMessage

internal class NewPins : CorePlugin(Manifest("NewPins")) {
    override val isHidden: Boolean = true
    override val isRequired: Boolean = true

    companion object {
        const val PIN_MESSAGES_PERMISSION = 1L shl 51
    }

    override fun start(context: Context) {
        patchPinnedMessages()
        patchMessageContext()
        patchPermissionsEditor()
    }

    override fun stop(context: Context) { }

    private data class ChannelPinsExtension(
        var isFetching: Boolean = false,
        var hasMore: Boolean = true,
        var oldestPinTimestamp: String? = null,
    )
    private val pinExtensionData = HashMap<Long, ChannelPinsExtension>()
    private val WidgetChatListAdapter.extension get() = pinExtensionData.getOrPut(this.data.channelId) { ChannelPinsExtension() }

    private data class GetChannelPinsResponse(
        val items: List<MessagePin>,
        @SerializedName("has_more") val hasMore: Boolean,
    ) {
        data class MessagePin(
            @SerializedName("pinned_at") val pinnedAt: String,
            val message: ApiMessage,
        )
    }

    // Patches pinned messages to show more than 50 pins
    private fun patchPinnedMessages() {
        // The event handler doesn't store the adapter, so we have to patch and store it to use it
        val handlerAdapterMap = WeakHashMap<WidgetChatListAdapter.EventHandler, WidgetChatListAdapter>()
        // The widget's adapter is private. Reflection is also possible, but might as well use a weakmap
        val widgetAdapterMap = WeakHashMap<WidgetChannelPinnedMessages, WidgetChatListAdapter>()

        // The adapter conveniently gets passed here right after its constructed, so we store it in our weakmaps
        patcher.after<WidgetChannelPinnedMessages>(
            "addThreadSpineItemDecoration",
            WidgetChatListAdapter::class.java
        ) { (_, adapter: WidgetChatListAdapter) ->
            handlerAdapterMap[adapter.eventHandler] = adapter
            widgetAdapterMap[this] = adapter
        }

        // Listens to state updates and then fetches new pins when the bottom (state.isAtTop) is reached
        patcher.after<WidgetChannelPinnedMessages.ChannelPinnedMessagesAdapterEventHandler>(
            "onInteractionStateUpdated",
            StoreChat.InteractionState::class.java,
        ) { (_, state: StoreChat.InteractionState) ->
            val adapter = handlerAdapterMap[this] ?: return@after

            // Yes, top means bottom
            if (state.isAtTop) {
                requestMorePins(adapter)
            }
        }

        // Replaces the old endpoint with the new one to get extra information like pin timestamps and hasMore
        patcher.instead<RestAPI>("getChannelPins", Long::class.javaPrimitiveType!!) { (_, channelId: Long) ->
            Observable.h0<List<ApiMessage>> { emitter -> // Observable.create(onSubscribe: (emitter: Subscriber) -> Unit)
                val req = Http.Request.newDiscordRNRequest("/channels/${channelId}/messages/pins")
                val res = req.execute()
                if (!res.ok()) {
                    logger.errorToast("Error while fetching pins: ${res.statusCode}: ${res.statusMessage}", null)
                    emitter.onNext(listOf())
                } else {
                    val data = res.json(GsonUtils.gsonRestApi, GetChannelPinsResponse::class.java)
                    val extension = pinExtensionData.getOrPut(channelId) { ChannelPinsExtension() }
                    extension.hasMore = data.hasMore
                    extension.oldestPinTimestamp = data.items.last().pinnedAt
                    emitter.onNext(data.items.map { it.message })
                }
                emitter.onCompleted()
            }
        }

        // Adds a cute loading icon when there's more to be loaded
        patcher.before<WidgetChannelPinnedMessages>(
            "configureUI",
            WidgetChannelPinnedMessages.Model::class.java
        ) { (param, model: WidgetChannelPinnedMessages.Model) ->
            val adapter = widgetAdapterMap[this] ?: return@before

            if (adapter.extension.hasMore) {
                param.args[0] = model.copy(
                    model.channel,
                    model.guild,
                    model.userId,
                    model.channelNames,
                    model.list + LoadingEntry(),
                    model.myRoleIds,
                    model.channelId,
                    model.guildId,
                    model.oldestMessageId,
                    model.newMessagesMarkerMessageId,
                    model.isSpoilerClickAllowed,
                )
            }
        }
    }

    // Fetch more pins
    private fun requestMorePins(adapter: WidgetChatListAdapter) {
        if (adapter.data.list.size < 50) return
        if (adapter.extension.isFetching) return
        if (!adapter.extension.hasMore) return
        if (adapter.extension.oldestPinTimestamp == null) return

        adapter.extension.isFetching = true

        Utils.threadPool.execute {
            try {
                val encodedTimestamp = URLEncoder.encode(adapter.extension.oldestPinTimestamp, "UTF-8")
                val url = "/channels/${adapter.data.channelId}/messages/pins?before=${encodedTimestamp}"
                val data = Http.Request.newDiscordRNRequest(url)
                    .execute()
                    .json(GsonUtils.gsonRestApi, GetChannelPinsResponse::class.java)

                val olderPins = data.items.map { Message(it.message) }
                adapter.extension.hasMore = data.hasMore

                val pinStore = StoreStream.getPinnedMessages()
                pinStore.dispatcher.schedule {
                    @Suppress("UNCHECKED_CAST")
                    val lastPins = StorePinnedMessages.`access$getPinnedMessages$p`(pinStore)[adapter.data.channelId] as List<Message>
                    StorePinnedMessages.`access$handlePinnedMessagesLoaded`(
                        StoreStream.getPinnedMessages(),
                        adapter.data.channelId,
                        lastPins + olderPins,
                    )
                    adapter.extension.isFetching = false
                }
            } catch (exception: Throwable) {
                logger.error("Failed to fetch new pins", exception)
            }
        }
    }

    // Supports the new Pin Messages permission
    private fun patchMessageContext() {
        // Patches the message context to determine pin ability based on the new permission
        patcher.after<ManageMessageContext.Companion>(
            "from",
            Message::class.java,
            Long::class.javaObjectType,
            MeUser::class.java,
            Int::class.javaObjectType,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
        ) { (
                param,
                /* message */ _: Message,
                permissions: Long?,
                /* meUser */ _: MeUser,
                /* guildMfaLevel */ _: Int,
                isPrivateChannel: Boolean,
                isSystemDM: Boolean,
                isArchivedThread: Boolean,
            ) ->
            val result = param.result as ManageMessageContext

            // TODO: After 2026/01/12, users will no longer be able to pin messages with the
            // Manage Messages permission, and this line should be removed around the same time.
            // https://discord.com/developers/docs/change-log#pin-permission-split
            if (result.canTogglePinned) return@after

            val isPrivateDM = isPrivateChannel && !isSystemDM
            val isPinPermitted = isPrivateDM || PermissionUtils.can(PIN_MESSAGES_PERMISSION, permissions)
            val canPin = isPinPermitted && !isArchivedThread
            param.result = ManageMessageContext(
                result.canManageMessages,
                result.canEdit,
                result.canDelete,
                result.canAddReactions,
                canPin,
                result.canMarkUnread
            )
        }
    }

    // Patches the permissions editor to show new permission
    private fun patchPermissionsEditor() {
        val pinMessagesCheckboxViewId = View.generateViewId()

        // Patches the editor to add our new pin messages checkbox and change the information
        // regarding the manage messages permission
        patcher.before<WidgetChannelSettingsEditPermissions>(
            "onViewBound",
            View::class.java,
        ) { (_, view: View) ->
            val layoutId = Utils.getResId("channel_permissions_text_container", "id")
            val layout: LinearLayout = view.findViewById(layoutId)
            val manageMessagesCheckboxId = Utils.getResId("channel_permission_text_manage_messages", "id")
            val manageMessagesCheckbox: TernaryCheckBox = view.findViewById(manageMessagesCheckboxId)
            val index = layout.indexOfChild(manageMessagesCheckbox) + 1

            // TODO: Should be changed after 2026/01/12, or when official client does
            manageMessagesCheckbox.setLabel("Manage Messages ⚠")
            manageMessagesCheckbox.setSubtext(
                "Members with this permission can delete messages by other members or pin any message.*\n\n" +
                "* Pinning messages now has a separate permission. This setting's behaviour will change soon."
            )

            TernaryCheckBox(view.context, null).addTo(layout, index) {
                id = pinMessagesCheckboxViewId
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

                this.k.e.run { /* label.run */
                    text = "Pin Messages"
                    visibility = View.VISIBLE
                }

                this.k.f.run { /* subtext.run */
                    text = "Allows members to pin or unpin any message."
                    visibility = View.VISIBLE
                }
            }
        }

        // Patches logic to map our new permission checkbox to its value
        patcher.before<WidgetChannelSettingsEditPermissions.Companion>(
            "getPermission",
            Int::class.javaPrimitiveType!!,
        ) { (param, resId: Int) ->
            if (resId == pinMessagesCheckboxViewId) {
                param.result = PIN_MESSAGES_PERMISSION
            }
        }

        // Patches logic that handles configuring initial state of checkboxes
        patcher.after<`WidgetChannelSettingsEditPermissions$permissionCheckboxes$2`>("invoke") { param ->
            val widget = this.`this$0`
            val view = widget.view
            if (view == null) {
                logger.error(IllegalStateException("View was not initialised"))
                return@after
            }

            val pinMessagesCheckbox: TernaryCheckBox? = view.findViewById(pinMessagesCheckboxViewId)
            if (pinMessagesCheckbox == null) {
                logger.error(IllegalStateException("Pin messages checkbox not found"))
                return@after
            }

            @Suppress("UNCHECKED_CAST")
            val checkboxes = (param.result as List<TernaryCheckBox>).toMutableList()
            val manageMessagesIndex = checkboxes.indexOf(view.findViewById(Utils.getResId("channel_permission_text_manage_messages", "id")))
            checkboxes.add(manageMessagesIndex + 1, pinMessagesCheckbox)

            param.result = checkboxes
        }
    }
}

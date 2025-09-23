package com.aliucord.coreplugins

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout
import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.GsonUtils
import com.aliucord.utils.RxUtils
import com.aliucord.utils.SerializedName
import com.aliucord.utils.ViewUtils.addTo
import com.aliucord.utils.ViewUtils.label
import com.aliucord.utils.ViewUtils.subtext
import com.aliucord.wrappers.GuildRoleWrapper.Companion.permissions
import com.discord.models.message.Message
import com.discord.models.user.MeUser
import com.discord.restapi.RestAPIParams
import com.discord.stores.*
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.permissions.ManageMessageContext
import com.discord.utilities.permissions.PermissionUtils
import com.discord.utilities.rest.RestAPI
import com.discord.views.CheckedSetting
import com.discord.views.TernaryCheckBox
import com.discord.widgets.channels.permissions.WidgetChannelSettingsEditPermissions
import com.discord.widgets.channels.permissions.`WidgetChannelSettingsEditPermissions$permissionCheckboxes$2`
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapter
import com.discord.widgets.chat.list.entries.LoadingEntry
import com.discord.widgets.chat.pins.WidgetChannelPinnedMessages
import com.discord.widgets.servers.WidgetServerSettingsEditRole
import com.discord.widgets.servers.WidgetServerSettingsEditRole.Model.ManageStatus
import com.lytefast.flexinput.R
import rx.Observable
import java.net.URLEncoder
import java.util.Date
import java.util.WeakHashMap
import com.discord.api.message.Message as ApiMessage

private const val PIN_MESSAGES_PERMISSION = 1L shl 51

// After 2026/01/12, users will no longer be able to pin messages with the
// Manage Messages permission.
// https://discord.com/developers/docs/change-log#pin-permission-split
// This constant is the Unix timestamp in ms at 2026/01/11, one day before the
// breaking change. Code relating to this constant can be safely removed after
// the switch.
private const val MIGRATION_DEADLINE = 1768003200000L
private val isPastDeadline get() = Date().time > MIGRATION_DEADLINE

// TODO: Post migration strings are made up, and might differ from official clients later.
private val MANAGE_MESSAGES_TEXT = if (!isPastDeadline) {
    "Manage Messages ⚠"
} else {
    "Manage Messages"
}

private val MANAGE_MESSAGES_SUBTEXT = if (!isPastDeadline) {
    SpannableStringBuilder().apply {
        append("Members with this permission can delete messages by other members or pin any message.*\n\n")
        append(
            "* Pinning messages now has a separate permission. This setting's behaviour will change soon.",
            ForegroundColorSpan(Utils.appContext.getColor(R.c.status_yellow)),
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
} else {
    "Members with this permission can delete messages by other members."
}

private const val PIN_MESSAGES_TEXT = "Pin Messages"
private const val PIN_MESSAGES_SUBTEXT = "Allows members to pin or unpin any message."

internal class NewPins : CorePlugin(Manifest("NewPins")) {
    override val isHidden: Boolean = true
    override val isRequired: Boolean = true

    override fun start(context: Context) {
        patchPinnedMessages()
        patchMessageContext()
        patchChannelPermissionsEditor()
        patchRolePermissionsEditor()
    }

    override fun stop(context: Context) { }

    private data class PinStateExtra(
        var isFetching: Boolean = false,
        var hasMore: Boolean = true,
        var oldestPinTimestamp: String? = null,
    ) {
        companion object {
            private val channelPins = HashMap<Long, PinStateExtra>()
            fun of(channelId: Long) = channelPins.getOrPut(channelId) { PinStateExtra() }
        }
    }

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
            RxUtils.create<List<ApiMessage>> { subscriber ->
                val req = Http.Request.newDiscordRNRequest("/channels/${channelId}/messages/pins")
                val res = req.execute()
                if (!res.ok()) {
                    logger.errorToast("Error while fetching pins: ${res.statusCode}: ${res.statusMessage}", null)
                    subscriber.onNext(listOf())
                } else {
                    val data = res.json(GsonUtils.gsonRestApi, GetChannelPinsResponse::class.java)
                    val state = PinStateExtra.of(channelId)
                    state.hasMore = data.hasMore
                    state.oldestPinTimestamp = data.items.last().pinnedAt
                    subscriber.onNext(data.items.map { it.message })
                }
                subscriber.onCompleted()
            }
        }

        val modelListField = WidgetChannelPinnedMessages.Model::class.java.getDeclaredField("list")
            .apply { isAccessible = true }
        // Adds a cute loading icon when there's more to be loaded
        patcher.before<WidgetChannelPinnedMessages>(
            "configureUI",
            WidgetChannelPinnedMessages.Model::class.java
        ) { (_, model: WidgetChannelPinnedMessages.Model) ->
            if (PinStateExtra.of(model.channelId).hasMore) {
                modelListField.set(model, model.list + LoadingEntry())
            }
        }
    }

    // Fetch more pins
    private fun requestMorePins(adapter: WidgetChatListAdapter) {
        if (adapter.data.list.size < 50) return

        val state = PinStateExtra.of(adapter.data.channelId)
        if (state.isFetching) return
        if (!state.hasMore) return
        if (state.oldestPinTimestamp == null) return

        state.isFetching = true

        Utils.threadPool.execute {
            try {
                val encodedTimestamp = URLEncoder.encode(state.oldestPinTimestamp, "UTF-8")
                val url = "/channels/${adapter.data.channelId}/messages/pins?before=${encodedTimestamp}"
                val data = Http.Request.newDiscordRNRequest(url)
                    .execute()
                    .json(GsonUtils.gsonRestApi, GetChannelPinsResponse::class.java)

                val olderPins = data.items.map { Message(it.message) }
                state.hasMore = data.hasMore

                val pinStore = StoreStream.getPinnedMessages()
                pinStore.dispatcher.schedule {
                    @Suppress("UNCHECKED_CAST")
                    val lastPins = StorePinnedMessages.`access$getPinnedMessages$p`(pinStore)[adapter.data.channelId] as List<Message>
                    StorePinnedMessages.`access$handlePinnedMessagesLoaded`(
                        StoreStream.getPinnedMessages(),
                        adapter.data.channelId,
                        lastPins + olderPins,
                    )
                    state.isFetching = false
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

            // TODO: This line can be removed after 2026/01/12
            if (result.canTogglePinned && !isPastDeadline) return@after

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

    // Patches the role permissions editor to show new permission
    private fun patchRolePermissionsEditor() {
        val pinMessagesCheckboxViewId = View.generateViewId()

        // Patches the editor to add the new permission checkbox
        patcher.before<WidgetServerSettingsEditRole>(
            "onViewBound",
            View::class.java,
        ) { (_, view: View) ->
            val manageMessagesCheckboxId = Utils.getResId("role_settings_manage_messages", "id")
            val manageMessagesCheckbox = view.findViewById<CheckedSetting>(manageMessagesCheckboxId)
            val layout = manageMessagesCheckbox.parent as LinearLayout
            val index = layout.indexOfChild(manageMessagesCheckbox) + 1

            manageMessagesCheckbox.setText(MANAGE_MESSAGES_TEXT)
            manageMessagesCheckbox.setSubtext(MANAGE_MESSAGES_SUBTEXT)

            CheckedSetting(view.context, null).addTo(layout, index) {
                id = pinMessagesCheckboxViewId
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

                label.run {
                    setTextColor(ColorCompat.getThemedColor(view.context, R.b.primary_100))
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.d.uikit_textsize_large))
                    text = PIN_MESSAGES_TEXT
                    layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                        marginStart = 0
                    }
                }
                subtext.run {
                    setTextColor(ColorCompat.getThemedColor(view.context, R.b.primary_300))
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.d.uikit_textsize_medium))
                    text = PIN_MESSAGES_SUBTEXT
                    layoutParams = (layoutParams as ConstraintLayout.LayoutParams).apply {
                        marginStart = 0
                    }
                }
            }
        }

        // Patches the method responsible for handling logic related to permissions (checked state, update request, etc..)
        patcher.after<WidgetServerSettingsEditRole>(
            "setupPermissionsSettings",
            WidgetServerSettingsEditRole.Model::class.java
        ) { (_, model: WidgetServerSettingsEditRole.Model) ->
            val view = this.view
            if (view == null) {
                logger.error(IllegalStateException("View was not initialised"))
                return@after
            }
            val pinMessagesCheckbox = view.findViewById<CheckedSetting?>(pinMessagesCheckboxViewId)
            if (pinMessagesCheckbox == null) {
                logger.error(IllegalStateException("Pin messages checkbox not found"))
                return@after
            }

            val permissionEnabled = PermissionUtils.can(PIN_MESSAGES_PERMISSION, model.role.permissions)
            pinMessagesCheckbox.isChecked = permissionEnabled
            val manageStatus = model.manageStatus ?: ManageStatus.NO_MANAGE_ROLES_PERMISSION
            if (manageStatus == ManageStatus.CAN_MANAGE_ADMIN) {
                enableRoleSetting(pinMessagesCheckbox, model)
            } else if (manageStatus == ManageStatus.CAN_MANAGE_CONDITIONAL) {
                val selfCan = PermissionUtils.can(PIN_MESSAGES_PERMISSION, model.myPermissions)
                if (!selfCan) {
                    // You don't have permission to change this
                    pinMessagesCheckbox.b(R.h.help_missing_permission) // onClickToastText(stringId)
                } else if (model.isSingular(PIN_MESSAGES_PERMISSION) && permissionEnabled) {
                    // Removing the permission from the role would remove it from yourself
                    pinMessagesCheckbox.b(R.h.help_singular_permission) // onClickToastText(stringId)
                } else {
                    enableRoleSetting(pinMessagesCheckbox, model)
                }
            } else {
                val toastMessage = WidgetServerSettingsEditRole.`access$getLockMessage`(this, model, false)
                pinMessagesCheckbox.c(toastMessage) // onClickToastText(string)
            }
        }
    }

    // Ref: WidgetServerSettingsEditRole$enableSetting$1
    private fun WidgetServerSettingsEditRole.enableRoleSetting(checkbox: CheckedSetting, model: WidgetServerSettingsEditRole.Model) {
        checkbox.e { // setOnClickListener
            val binding = WidgetServerSettingsEditRole.`access$getBinding$p`(this)
            binding.b.clearFocus() // TextInputLayout.clearFocus()
            val roleUpdateParams = RestAPIParams.Role.createWithRole(model.role)
            roleUpdateParams.permissions = model.role.permissions xor PIN_MESSAGES_PERMISSION
            WidgetServerSettingsEditRole.`access$patchRole`(this, model.guildId, roleUpdateParams)
        }
    }

    // Patches the channel permissions editor to show new permission
    private fun patchChannelPermissionsEditor() {
        val pinMessagesCheckboxViewId = View.generateViewId()

        // Patches the editor to add our new pin messages checkbox and change the information
        // regarding the manage messages permission
        patcher.before<WidgetChannelSettingsEditPermissions>(
            "onViewBound",
            View::class.java,
        ) { (_, view: View) ->
            val layoutId = Utils.getResId("channel_permissions_text_container", "id")
            val layout = view.findViewById<LinearLayout>(layoutId)
            val manageMessagesCheckboxId = Utils.getResId("channel_permission_text_manage_messages", "id")
            val manageMessagesCheckbox = view.findViewById<TernaryCheckBox>(manageMessagesCheckboxId)
            val index = layout.indexOfChild(manageMessagesCheckbox) + 1

            manageMessagesCheckbox.setLabel(MANAGE_MESSAGES_TEXT)
            manageMessagesCheckbox.setSubtext(MANAGE_MESSAGES_SUBTEXT)

            TernaryCheckBox(view.context, null).addTo(layout, index) {
                id = pinMessagesCheckboxViewId
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

                this.k.e.run { /* label.run */
                    text = PIN_MESSAGES_TEXT
                    visibility = View.VISIBLE
                }

                this.k.f.run { /* subtext.run */
                    text = PIN_MESSAGES_SUBTEXT
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

            val pinMessagesCheckbox = view.findViewById<TernaryCheckBox?>(pinMessagesCheckboxViewId)
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

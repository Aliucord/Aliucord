package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.ViewUtils.addTo
import com.discord.models.message.Message
import com.discord.models.user.MeUser
import com.discord.utilities.permissions.ManageMessageContext
import com.discord.utilities.permissions.PermissionUtils
import com.discord.views.TernaryCheckBox
import com.discord.widgets.channels.permissions.WidgetChannelSettingsEditPermissions
import com.discord.widgets.channels.permissions.`WidgetChannelSettingsEditPermissions$permissionCheckboxes$2`

internal class NewPins : CorePlugin(Manifest("NewPins")) {
    override val isHidden: Boolean = true
    override val isRequired: Boolean = true

    companion object {
        const val PIN_MESSAGES_PERMISSION = 1L shl 51
    }

    override fun start(context: Context) {
        patchMessageContext()
        patchPermissionsEditor()
    }

    override fun stop(context: Context) { }

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
            param.setResult(
                ManageMessageContext(
                    result.canManageMessages,
                    result.canEdit,
                    result.canDelete,
                    result.canAddReactions,
                    canPin,
                    result.canMarkUnread
                )
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
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

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

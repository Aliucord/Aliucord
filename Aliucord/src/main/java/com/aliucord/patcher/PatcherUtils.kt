package com.aliucord.patcher

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.aliucord.Logger
import com.lytefast.flexinput.R
import com.aliucord.api.PatcherAPI
import com.aliucord.utils.DimenUtils
import com.discord.databinding.WidgetChannelsListItemActionsBinding
import com.discord.databinding.WidgetChatListActionsBinding
import com.discord.widgets.channels.list.WidgetChannelsListItemChannelActions
import com.discord.widgets.chat.list.actions.WidgetChatListActions

@Suppress("Unused")
object PatcherUtils {
    // Patcher for the utils
    private val patcher = PatcherAPI(Logger("PatcherUtils"))

    // Channel actions menu
    private val channelActions = arrayListOf<ChannelAction>()
    private var channelActionsPatched = false
    private var channelActionsUnpatch: Runnable? = null

    private val getChatListChannelActionsBindingMethod = WidgetChannelsListItemChannelActions::class.java.getDeclaredMethod("getBinding")
        .apply { isAccessible = true }

    @JvmStatic
    fun addChannelAction(data: ChannelAction): () -> Unit {
        // Add to list
        channelActions.add(data)
        // Patch if not already patched
        if (!channelActionsPatched) {
            channelActionsUnpatch = patcher.after<WidgetChannelsListItemChannelActions>("configureUI", WidgetChannelsListItemChannelActions.Model::class.java) {
                val nestedScrollView = this.requireView() as NestedScrollView
                val layout = nestedScrollView.getChildAt(0) as LinearLayout
                val binding = getChatListChannelActionsBindingMethod.invoke(this) as WidgetChannelsListItemActionsBinding
                val view = binding.j
                val param = view.layoutParams
                val params = LinearLayout.LayoutParams(param.width, param.height)
                params.leftMargin = DimenUtils.dpToPx(20)
                channelActions.forEach { action ->
                    if (
                        action.condition != null && action.condition.invoke(it.args[0] as WidgetChannelsListItemChannelActions.Model)
                    ) return@after
                    val tw = TextView(view.context, null, 0, R.i.UiKit_Settings_Item_Icon)
                    tw.text = action.text
                    tw.setCompoundDrawablesRelativeWithIntrinsicBounds(action.icon, null, null, null)
                    tw.layoutParams = view.layoutParams
                    tw.id = View.generateViewId()
                    tw.setOnClickListener { v -> action.onClick.invoke(v, it.args[0] as WidgetChannelsListItemChannelActions.Model) }
                    if (action.onLongClick != null)
                        tw.setOnLongClickListener { v -> action.onLongClick.invoke(v, it.args[0] as WidgetChannelsListItemChannelActions.Model) }
                    layout.addView(tw)
                }
            }
            channelActionsPatched = true
        }
        return {
            this.removeChannelAction(data)
        }
    }

    @JvmStatic
    fun removeChannelAction(data: ChannelAction) {
        // Remove from list
        channelActions.remove(data)
        // If no more actions, unpatch because it is not needed
        val unpatch = channelActionsUnpatch // Null safety moment
        if (channelActions.size < 1 && unpatch != null) unpatch.run()
    }

    // Message actions menu
    private val messageActions = arrayListOf<MessageAction>()
    private var messageActionsPatched = false
    private var messageActionsUnpatch: Runnable? = null

    private val getChatListActionsBindingMethod = WidgetChatListActions::class.java.getDeclaredMethod("getBinding")
        .apply { isAccessible = true }

    @JvmStatic
    fun addMessageAction(data: MessageAction): () -> Unit {
        // Add to list
        messageActions.add(data)
        // Patch if not already patched
        if (!messageActionsPatched) {
            messageActionsUnpatch = patcher.after<WidgetChatListActions>("configureUI", WidgetChatListActions.Model::class.java) {
                val nestedScrollView = this.requireView() as NestedScrollView
                val layout = nestedScrollView.getChildAt(0) as LinearLayout
                val binding = getChatListActionsBindingMethod.invoke(this) as WidgetChatListActionsBinding
                val view = binding.j
                val param = view.layoutParams
                val params = LinearLayout.LayoutParams(param.width, param.height)
                params.leftMargin = DimenUtils.dpToPx(20)
                messageActions.forEach { action ->
                    if (
                        action.condition != null && action.condition.invoke(it.args[0] as WidgetChatListActions.Model)
                    ) return@after
                    val tw = TextView(view.context, null, 0, R.i.UiKit_Settings_Item_Icon)
                    tw.text = action.text
                    tw.setCompoundDrawablesRelativeWithIntrinsicBounds(action.icon, null, null, null)
                    tw.layoutParams = view.layoutParams
                    tw.id = View.generateViewId()
                    tw.setOnClickListener { v -> action.onClick.invoke(v, it.args[0] as WidgetChatListActions.Model) }
                    if (action.onLongClick != null)
                        tw.setOnLongClickListener { v -> action.onLongClick.invoke(v, it.args[0] as WidgetChatListActions.Model) }
                    layout.addView(tw)
                }
            }
            messageActionsPatched = true
        }
        return {
            this.removeMessageAction(data)
        }
    }

    @JvmStatic
    fun removeMessageAction(data: MessageAction) {
        // Remove from list
        messageActions.remove(data)
        // If no more actions, unpatch because it is not needed
        val unpatch = messageActionsUnpatch // Null safety moment
        if (messageActions.size < 1 && unpatch != null) unpatch.run()
    }

    // Action data
    data class ChannelAction @JvmOverloads constructor(
        val text: String,
        val icon: Drawable,
        val onClick: (view: View, model: WidgetChannelsListItemChannelActions.Model) -> Any,
        val onLongClick: ((view: View, model: WidgetChannelsListItemChannelActions.Model) -> Boolean)? = null,
        val condition: ((model: WidgetChannelsListItemChannelActions.Model) -> Boolean)? = null
    )
    data class MessageAction @JvmOverloads constructor(
        val text: String,
        val icon: Drawable,
        val onClick: (view: View, model: WidgetChatListActions.Model) -> Any,
        val onLongClick: ((view: View, model: WidgetChatListActions.Model) -> Boolean)? = null,
        val condition: ((model: WidgetChatListActions.Model) -> Boolean)? = null
    )
}

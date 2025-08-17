package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aliucord.Utils
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.aliucord.utils.DimenUtils
import com.discord.utilities.drawable.DrawableCompat
import com.discord.widgets.channels.list.`WidgetChannelsListItemChannelActions$binding$2`
import com.discord.widgets.chat.list.actions.`WidgetChatListActions$binding$2`
import com.lytefast.flexinput.R

internal class AlignThreads : CorePlugin(Manifest("AlignThreads")) {
    init {
        manifest.description = "Adjusts the Threads icon in the Chat actions menu"
    }

    override fun start(context: Context) {
        patcher.after<`WidgetChannelsListItemChannelActions$binding$2`>("invoke", View::class.java) { (_, view: View) ->
            adjustThreadIcon(
                rootView = view,
                textViewId = Utils.getResId("text_action_thread_browser", "id"),
                themed = true,
            )
        }
        patcher.after<`WidgetChatListActions$binding$2`>("invoke", View::class.java) { (_, view: View) ->
            adjustThreadIcon(
                rootView = view,
                textViewId = Utils.getResId("dialog_chat_actions_start_thread", "id"),
                themed = false,
            )
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()

    private fun adjustThreadIcon(rootView: View, textViewId: Int, themed: Boolean) {
        val iconId = if (themed) {
            DrawableCompat.getThemedDrawableRes(rootView.context, R.b.ic_thread)
        } else {
            R.e.ic_thread
        }
        val icon = ContextCompat.getDrawable(rootView.context, iconId)!!.apply {
            val size = DimenUtils.dpToPx(24)
            setBounds(0, 0, size, size)
        }

        val textView = rootView.findViewById<TextView>(textViewId)
        textView.setCompoundDrawables(icon, null, null, null)
    }
}

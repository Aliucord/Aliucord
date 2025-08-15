package com.aliucord.coreplugins

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
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
        patcher.after<`WidgetChannelsListItemChannelActions$binding$2`>("invoke", View::class.java) { (_, view: View) -> adjustIcon(view, true, true) }
        patcher.after<`WidgetChatListActions$binding$2`>("invoke", View::class.java) { (_, view: View) -> adjustIcon(view, false, false) }
    }

    private fun adjustIcon(view: View, res_id: Boolean, res: Boolean) {
        val res_id = if (res_id) "text_action_thread_browser" else "dialog_chat_actions_start_thread"
        val id = Utils.getResId(res_id, "id")
        val textview = view.findViewById<TextView>(id)
        val size = DimenUtils.dpToPx(24)
        val res = if (res) DrawableCompat.getThemedDrawableRes(textview.context, R.b.ic_thread) else R.e.ic_thread
        val icon = ContextCompat.getDrawable(textview.context, res)!!
        icon.setBounds(0, 0, size, size)
        textview.setCompoundDrawables(icon, null, null, null)
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}

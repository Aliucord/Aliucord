package com.aliucord.fragments

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils
import com.discord.app.AppDialog
import com.lytefast.flexinput.R

/**
 * Creates a dialog similar to the language picker, allows you to supply a list of options for a user to select from.
 */
class SelectDialog() : AppDialog(Utils.getResId("widget_settings_language_select", "layout")) {
    private inner class Adapter(private val items: Array<String>) : RecyclerView.Adapter<Adapter.ViewHolder?>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(TextView(parent.context, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            })
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(position)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class ViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
            fun bind(position: Int) {
                textView.run {
                    text = items[position]
                    setOnClickListener { _ ->
                        onItemPicked(position)
                    }
                }
            }
        }
    }

    /***
     * Called when an item is selected
     */
    var onResultListener: ((Int) -> Unit)? = null

    /***
     * Items for the user to pick from
     */
    var items: Array<String> = arrayOf()

    /***
     * Title displayed above the item list
     */
    var title: String = "Select an item"

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        val p = DimenUtils.dpToPx(16)
        val rv = view.findViewById(Utils.getResId("settings_language_select_list", "id")) as RecyclerView
        rv.adapter = Adapter(items)
        (rv.parent as ViewGroup).removeViewAt(0)
        val titleTv = TextView(view.context, null, 0, R.i.UiKit_Sheet_Header_Title).apply {
            text = title
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(p, p, p, p)
        }
        (rv.parent as ViewGroup).addView(titleTv, 0)
    }

    private fun onItemPicked(position: Int) {
        onResultListener?.invoke(position)
        dismiss()
    }

}

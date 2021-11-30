package com.aliucord.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.annotation.NonNull
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils
import com.discord.app.AppDialog
import com.lytefast.flexinput.R

class SelectDialog() : AppDialog(Utils.getResId("widget_settings_language_select", "layout")) {
    inner class Adapter(private val items: Array<String>) : RecyclerView.Adapter<Adapter.ViewHolder?>() {
        @NonNull
        override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(TextView(parent.getContext(), null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            })
        }

        override fun onBindViewHolder(@NonNull holder: ViewHolder, position: Int) {
            holder.bind(position)
        }

        override fun getItemCount() : Int {
            return items.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(position: Int) {
                (itemView as TextView).run {
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
        set
        get

    /***
     * Items for the user to pick from
     */
    var items: Array<String> = arrayOf()
        set
        get

    /***
     * Title displayed above the item list
     */
    var title: String = "Title"
        set
        get

    override fun onViewBound(view: View) {
        super.onViewBound(view)
        val p: Int = DimenUtils.dpToPx(16)
        val rv: RecyclerView = view.findViewById(Utils.getResId("settings_language_select_list", "id")) as RecyclerView
        rv.setAdapter(Adapter(items))
        (rv.getParent() as ViewGroup).removeViewAt(0)
        val titleTv = TextView(view.getContext(), null, 0, R.i.UiKit_Sheet_Header_Title).apply {
            text = title
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(p, p, p, p)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        (rv.getParent() as ViewGroup).addView(titleTv, 0)
    }

    private fun onItemPicked(position: Int) {
        onResultListener?.invoke(position)
        dismiss()
    }

}

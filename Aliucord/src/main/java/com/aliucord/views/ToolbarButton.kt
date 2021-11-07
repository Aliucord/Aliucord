package com.aliucord.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatImageButton
import com.aliucord.Utils
import com.lytefast.flexinput.R

class ToolbarButton(context: Context) :
    AppCompatImageButton(ContextThemeWrapper(context, R.i.UiKit_ImageView_Clickable), null, 0) {
    override fun setImageDrawable(drawable: Drawable?) {
        setImageDrawable(drawable, true)
    }

    fun setImageDrawable(drawable: Drawable?, forceTint: Boolean) {
        if (forceTint && drawable != null) {
            val drawable = drawable.mutate()
            Utils.tintToTheme(drawable)
        }
        super.setImageDrawable(drawable)
    }
}

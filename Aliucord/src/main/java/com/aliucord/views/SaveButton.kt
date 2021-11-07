/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.views

import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lytefast.flexinput.R

/** Discord Style Save Button  */
class SaveButton(context: Context) : LinearLayout(context) {
    val saveButton = FloatingActionButton(context).run {
        setImageDrawable(ContextCompat.getDrawable(context, R.e.icon_save))
        backgroundTintList = ColorStateList.valueOf(context.resources.getColor(R.c.brand, context.theme))
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1F)
        setHorizontalGravity(Gravity.RIGHT)
        setVerticalGravity(Gravity.BOTTOM)
        addView(this)
    }

}

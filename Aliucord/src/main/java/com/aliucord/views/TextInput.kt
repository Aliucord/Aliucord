/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.views

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.aliucord.Utils.getResId
import com.aliucord.Utils.tintToTheme
import com.aliucord.utils.DimenUtils.defaultCardRadius
import com.google.android.material.textfield.TextInputLayout


class TextInput @JvmOverloads constructor(
    context: Context,
    hint: CharSequence? = null,
    value: String? = null,
    textChangedListener: TextWatcher? = null,
    endIconOnClick: OnClickListener? = null
) : CardView(context) {
    var layout: TextInputLayout

    constructor(
        context: Context,
        hint: CharSequence?,
        value: String?,
        endIconOnClick: OnClickListener?
    ) : this(context, hint, value, null, endIconOnClick)

    /**
     * Returns the main edit text
     * @return EditText
     */
    val editText: EditText
        get() = (layout.getChildAt(0) as ViewGroup).getChildAt(0) as EditText

    /**
     * Returns the root layout
     * @return TextInputLayout
     */
    fun getRoot(): TextInputLayout {
        return layout
    }

    /**
     * Sets the hint message
     * @param hint The hint
     * @return self
     */
    fun setHint(hint: CharSequence): TextInput {
        layout.hint = hint
        return this
    }

    /**
     * Sets the hint message
     * @param hint The hint res id
     * @return self
     */
    fun setHint(@StringRes hint: Int): TextInput {
        layout.setHint(hint)
        return this
    }

    /**
     * Sets the end icon to the specified drawable and sets it tint to the users's chosen theme
     * @param icon End icon drawable
     * @return self
     */
    fun setThemedEndIcon(icon: Drawable): TextInput {
        layout.endIconDrawable = tintToTheme(icon.mutate())
        return this
    }

    /**
     * Sets the end icon to the specified drawable and sets it tint to the users's chosen theme
     * @param icon End icon drawable res id
     * @return self
     */
    fun setThemedEndIcon(@DrawableRes icon: Int): TextInput {
        layout.endIconDrawable =
            tintToTheme(ContextCompat.getDrawable(layout.context, icon))
        return this
    }

    init {
        val root = LinearLayout(context)
        LayoutInflater.from(context)
            .inflate(getResId("widget_change_guild_identity", "layout"), root)
        layout = root.findViewById<View>(getResId("set_nickname_text", "id")) as TextInputLayout
        (layout.parent as CardView).removeView(layout)
        addView(layout)
        setCardBackgroundColor(Color.TRANSPARENT)
        getRoot().hint = hint ?: "Enter Text"
        if (value != null && value.isNotEmpty()) editText.setText(value)
        //if(placeholder != null && !placeholder.isEmpty()) getEditText().setPlaceholder(placeholder);
        radius = defaultCardRadius.toFloat()
        getRoot().isEndIconVisible = false

        editText.addTextChangedListener(textChangedListener
            ?: object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    getRoot().isEndIconVisible = s.toString().isNotEmpty()
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }
            })
        getRoot().setEndIconOnClickListener(
            endIconOnClick ?: OnClickListener { editText.setText("") })
    }
}

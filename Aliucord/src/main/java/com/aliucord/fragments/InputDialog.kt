/*
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */
package com.aliucord.fragments

import android.annotation.SuppressLint
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.*
import com.aliucord.Utils.getResId
import com.discord.app.AppDialog
import com.discord.databinding.WidgetKickUserBinding
import com.discord.widgets.user.`WidgetKickUser$binding$2`
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.lytefast.flexinput.R
import java.util.*

/**
 * Creates a Input Dialog similar to the **Kick User** dialog.
 * This class offers convenient builder methods so you should usually not have to do any layouts manually.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class InputDialog : AppDialog(resId) {
    private var onDialogShownListener: IOnDialogShownListener? = null
    private var binding: WidgetKickUserBinding? = null
    private var title: CharSequence? = null
    private var description: CharSequence? = null
    private var placeholder: CharSequence? = null
    private var onCancelListener: View.OnClickListener? = null
    private var onOkListener: View.OnClickListener? = null
    private var inputType: Int? = null

    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        binding = `WidgetKickUser$binding$2`.INSTANCE.invoke(view)

        // Hide ugly redundant secondary "REASON FOR KICK" header
        (((view as LinearLayout).getChildAt(2) as ScrollView).getChildAt(0) as LinearLayout).getChildAt(1)
            .visibility = View.GONE

        header.text = if (title != null) title else "Input"
        body.text = if (description != null) description else "Please enter some text"
        body.movementMethod = LinkMovementMethod.getInstance()

        inputLayout.hint = placeholder ?: title ?: "Hint"

        if (inputType != null) inputLayout.editText?.inputType = inputType!!
        val buttonLayout = okButton.parent as LinearLayout
        // TODO: The button has no room to breathe - Discord moment, it doesn't even line up with the input field ~ Whatever, fix that
        with(buttonLayout) {
            setPadding(paddingLeft, paddingTop, paddingRight + 2, paddingBottom)
        }

        okButton.setBackgroundColor(
            view.getResources()
                .getColor(R.c.uikit_btn_bg_color_selector_brand, view.getContext().theme)
        )
        okButton.setOnClickListener(onOkListener ?: View.OnClickListener { dismiss() })

        cancelButton.setOnClickListener(onCancelListener ?: View.OnClickListener { dismiss() })
        if (onDialogShownListener != null) onDialogShownListener!!.onDialogShown(view)
    }

    /**
     * Returns the root layout of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a [NullPointerException] in other cases
     */
    val root: LinearLayout
        get() = binding!!.a

    /**
     * Returns the cancel [MaterialButton] of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a [NullPointerException] in other cases
     * @see .setOnCancelListener
     */
    val cancelButton: MaterialButton
        get() = binding!!.c

    /**
     * Returns the OK [MaterialButton] of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a [NullPointerException] in other cases
     * @see .setOnOkListener
     */
    val okButton: MaterialButton
        get() = binding!!.d

    /**
     * Returns the body of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a [NullPointerException] in other cases
     * @see .setDescription
     */
    val body: TextView
        get() = binding!!.b

    /**
     * Returns the header of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a [NullPointerException] in other cases
     * @see .setTitle
     */
    val header: TextView
        get() = binding!!.f

    /**
     * Returns the [TextInputLayout] of this dialog.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a [NullPointerException] in other cases
     * @see .setInputType
     * @see .getInput
     */
    val inputLayout: TextInputLayout
        get() = binding!!.e

    /**
     * Returns the input the user entered.
     * Should only be called from within onClickHandlers or onViewBound as it will likely throw a [NullPointerException] in other cases
     * @see .getInputLayout
     */
    val input: String
        get() = requireNotNull(inputLayout.editText).text.toString()

    /**
     * Sets the title of this dialog
     * @param title The description
     * @return Builder for chaining
     */
    fun setTitle(title: CharSequence?): InputDialog {
        this.title = title
        return this
    }

    /**
     * Sets the description of this dialog
     * @param description The description
     * @return Builder for chaining
     */
    fun setDescription(description: CharSequence?): InputDialog {
        this.description = description
        return this
    }

    /**
     * Sets the placeholder text for the input field (By default the title if set or "Text")
     * @param placeholder The placeholder text
     * @return Builder for chaining
     */
    fun setPlaceholderText(placeholder: CharSequence?): InputDialog {
        this.placeholder = placeholder
        return this
    }

    /**
     * Sets the [View.OnClickListener] that will be called when the OK button is pressed (By default simply closes this dialog)
     * @param listener The listener
     * @return Builder for chaining
     */
    fun setOnOkListener(listener: View.OnClickListener?): InputDialog {
        onOkListener = listener
        return this
    }

    /**
     * Sets the [View.OnClickListener] that will be called when the cancel button is pressed (By default simply closes this dialog)
     * @param listener The listener
     * @return Builder for chaining
     */
    fun setOnCancelListener(listener: View.OnClickListener?): InputDialog {
        onCancelListener = listener
        return this
    }

    /**
     * Sets the [android.text.InputType]
     * @param type The input type
     * @return Builder for chaining
     */
    fun setInputType(type: Int): InputDialog {
        inputType = type
        return this
    }

    /**
     * Sets the [InputDialog.IOnDialogShownListener] that will be called when the dialog is shown
     * @param listener Listener
     */
    fun setOnDialogShownListener(listener: IOnDialogShownListener?) {
        onDialogShownListener = listener
    }

    interface IOnDialogShownListener {
        fun onDialogShown(v: View?)
    }

    companion object {
        private val resId = getResId("widget_kick_user", "layout")
    }
}

/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2021 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

// Based on https://github.com/Vendicated/AliucordPlugins/blob/9d4c4637d9ac4cb6137916145b8ecc8e8231bb53/TextFilePreview/src/main/kotlin/dev/vendicated/aliucordplugins/textfilepreview/AttachmentPreviewWidget.kt

package com.aliucord.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.transition.TransitionManager
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import com.aliucord.PluginManager
import com.aliucord.Utils
import com.aliucord.utils.DimenUtils.dp
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.file.FileUtilsKt
import com.lytefast.flexinput.R
import java.io.File

@SuppressLint("SetTextI18n", "ViewConstructor")
class FailedPluginWidget(ctx: Context, private val file: File, private val reason: Any, onDelete: () -> Unit) : LinearLayout(ctx),
    View.OnClickListener {
    private val shortReason = reason.toString().let { if (reason is Throwable) "$it\n\n(Click file name for full info)" else it }
    private var fullReason = null as String?

    private val canExpand = reason is Throwable
    private var isExpanded = false

    private val mTextView: TextView
    private val mHeaderLayout: LinearLayout
    private val mFilenameView: TextView

    init {
        val dp8 = 8.dp
        val dp16 = 16.dp
        orientation = VERTICAL

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        setOnClickListener(this)

        mTextView = TextView(ctx, null, 0, R.i.UiKit_TextView).apply {
            setPadding(dp16, dp16, dp16, dp16)
            setTextIsSelectable(true)
        }

        mHeaderLayout = LinearLayout(ctx).apply {
            setPadding(dp16, dp8, dp16, dp8)
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            setBackgroundColor(ColorCompat.getThemedColor(ctx, R.b.colorBackgroundSecondaryAlt))
            mFilenameView = TextView(ctx, null, 0, R.i.UiKit_TextView_Bold).apply {
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                    weight = 1f
                }

                text = file.name + " (${FileUtilsKt.getSizeSubtitle(file.length())})"
                addView(this)
            }

            addView(ImageView(ctx).apply {
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).apply {
                    gravity = Gravity.END
                }
                setImageDrawable(ContextCompat.getDrawable(ctx, R.e.ic_delete_24dp)!!.mutate().apply {
                    setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal))
                })
                contentDescription = "Delete"

                setOnClickListener {
                    file.delete()
                    PluginManager.failedToLoad.remove(file)
                    onDelete()
                }
            })
        }
        addView(mHeaderLayout)
        addView(mTextView)

        configure()
    }

    private fun configure() {
        TransitionManager.beginDelayedTransition(mTextView.parent as ViewGroup)
        mTextView.text =
            if (isExpanded) fullReason ?: (reason as Throwable).stackTraceToString().also { fullReason = it }
            else shortReason
    }

    override fun onClick(view: View) {
        if (canExpand) {
            Utils.mainThread.post {
                isExpanded = !isExpanded
                configure()
            }
        }
    }
}

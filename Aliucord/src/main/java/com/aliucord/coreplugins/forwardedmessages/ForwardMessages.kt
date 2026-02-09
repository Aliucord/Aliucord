package com.aliucord.coreplugins.forwardedmessages

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.*
import android.os.Build
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.core.content.res.ResourcesCompat
import com.aliucord.coreplugins.forwardedmessages.MirroredDrawable
import com.aliucord.coreplugins.forwardedmessages.ForwardSettings
import com.aliucord.wrappers.messages.AttachmentWrapper
import com.aliucord.*
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.PreHook
import com.aliucord.utils.DimenUtils
import com.discord.api.role.GuildRole
import com.discord.databinding.WidgetIncomingShareBinding
import com.discord.utilities.SnowflakeUtils
import com.discord.utilities.captcha.CaptchaHelper
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.intent.IntentUtils
import com.discord.utilities.time.Clock
import com.discord.widgets.chat.list.ViewEmbedGameInvite
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.discord.widgets.share.WidgetIncomingShare
import com.discord.widgets.user.search.WidgetGlobalSearchModel
import com.google.android.material.appbar.AppBarLayout
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.lytefast.flexinput.R
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

data class ForwardedAttachment(val filename: String, val url: String, val type: String, val size: Long = 0) : java.io.Serializable

private const val EXTRA_CONTENT_INTENT = "com.aliucord.coreplugins.forwardedmessages.EXTRA_CONTENT"
private const val EXTRA_MESSAGE_ID_INTENT = "com.aliucord.coreplugins.forwardedmessages.EXTRA_MESSAGE_ID"
private const val EXTRA_CHANNEL_ID_INTENT = "com.aliucord.coreplugins.forwardedmessages.EXTRA_CHANNEL_ID"
private const val EXTRA_ATTACHMENTS_INTENT = "com.aliucord.coreplugins.forwardedmessages.EXTRA_ATTACHMENTS"

internal class ForwardMessages : CorePlugin(Manifest("ForwardMessages")) {

    init {
        settingsTab = SettingsTab(ForwardSettings.Sheet::class.java, SettingsTab.Type.BOTTOM_SHEET)
    }

    override fun start(context: Context) {
        val forwardId = View.generateViewId()

        val replyIcon: Drawable? = ContextCompat.getDrawable(context, R.e.ic_reply_24dp)?.mutate()
        replyIcon?.isAutoMirrored = true
        val forwardIcon = MirroredDrawable(replyIcon!!)

        val bindingReflection: Method = WidgetIncomingShare::class.java.getDeclaredMethod("getBinding")
        bindingReflection.isAccessible = true
        val modelCommentField: Field = WidgetIncomingShare.Model::class.java.getDeclaredField("comment")
        modelCommentField.isAccessible = true

        // Add "Forward" action to Action menu
        patcher.patch(WidgetChatListActions::class.java.getDeclaredMethod("configureUI", WidgetChatListActions.Model::class.java),
            PreHook { param ->
                val actions = param.thisObject as WidgetChatListActions
                val scrollView = actions.view as NestedScrollView
                val lay = scrollView.getChildAt(0) as LinearLayout

                if (lay.findViewById<View>(forwardId) == null) {
                    val tw = TextView(lay.context, null, 0, R.i.UiKit_Settings_Item_Icon)
                    tw.id = forwardId
                    tw.text = "Forward"
                    val mediumTypeface = ResourcesCompat.getFont(tw.context, Constants.Fonts.whitney_medium)
                    if (mediumTypeface != null) tw.typeface = mediumTypeface
                    val color = ColorCompat.getThemedColor(tw.context, R.b.colorInteractiveNormal)
                    forwardIcon.setTintList(android.content.res.ColorStateList.valueOf(color))
                    tw.setCompoundDrawablesRelativeWithIntrinsicBounds(forwardIcon, null, null, null)

                    val replyView = lay.findViewById<View>(Utils.getResId("dialog_chat_actions_reply", "id"))
                    val idx = if (replyView != null) lay.indexOfChild(replyView) + 1 else 5
                    lay.addView(tw, idx)

                    tw.setOnClickListener { _ ->
                        val model = param.args[0] as WidgetChatListActions.Model
                        val messageId = model.message.id
                        val messageContent = model.message.content
                        val channelId = model.channel.k()

                        // Collect attachment info
                        val attachments = try {
                            val attachmentsField = model.message.javaClass.getDeclaredField("attachments").apply { isAccessible = true }
                            attachmentsField.get(model.message) as? List<*>
                        } catch (_: Throwable) { null }
                        val attachmentInfo = attachments?.mapNotNull {
                            val msgAttachment = it as? com.discord.api.message.attachment.MessageAttachment
                            if (msgAttachment != null) {
                                val wrapper = AttachmentWrapper(msgAttachment)
                                ForwardedAttachment(
                                    filename = wrapper.filename,
                                    url = wrapper.proxyUrl ?: wrapper.url,
                                    type = wrapper.type.name,
                                    size = wrapper.size ?: 0L
                                )
                            } else null
                        }
                        val putExtra = Intent()
                            .putExtra(EXTRA_CONTENT_INTENT, messageContent)
                            .putExtra(EXTRA_MESSAGE_ID_INTENT, messageId)
                            .putExtra(EXTRA_CHANNEL_ID_INTENT, channelId)
                            .putExtra(EXTRA_ATTACHMENTS_INTENT, attachmentInfo?.let { ArrayList(it) } ?: ArrayList<ForwardedAttachment>())

                        Utils.mainThread.post {
                            if (context !is android.app.Activity) {
                                putExtra.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            Utils.openPage(context, WidgetIncomingShare::class.java, putExtra)
                            actions.dismiss()
                        }
                    }
                }
            })

        // Check incoming intent and modify labels
        patcher.patch(WidgetIncomingShare::class.java.getDeclaredMethod("initialize", WidgetIncomingShare.ContentModel::class.java),
            PreHook { param ->
                val share = param.thisObject as WidgetIncomingShare
                val intent = share.mostRecentIntent
                val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID_INTENT, 0)
                val channelId = intent.getLongExtra(EXTRA_CHANNEL_ID_INTENT, 0)
                val messageContent = intent.getStringExtra(EXTRA_CONTENT_INTENT)

                if (messageId == 0L || channelId == 0L) return@PreHook

                val binding: WidgetIncomingShareBinding = try {
                    bindingReflection.invoke(share) as WidgetIncomingShareBinding
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }

                val appBar = binding.a.getChildAt(0) as AppBarLayout
                val toolbar = appBar.getChildAt(0) as Toolbar
                toolbar.title = "Forward"

                val layout = binding.j.getChildAt(0) as LinearLayout
                val shareToText = layout.getChildAt(4) as TextView
                shareToText.text = "Forward To"

                val messagePreviewText = layout.getChildAt(0) as TextView
                messagePreviewText.text = "Optional Message"

                // Message Preview Header
                val messagePreviewCustom = TextView(layout.context, null, 0, com.lytefast.flexinput.R.i.UiKit_Search_Header)
                messagePreviewCustom.text = "Message Preview"
                layout.addView(messagePreviewCustom, 0)

                // Parsed Message Preview
                val previewText = SimpleDraweeSpanTextView(layout.context)
                val mediumTypeface = ResourcesCompat.getFont(layout.context, Constants.Fonts.whitney_medium)
                if (mediumTypeface != null) previewText.typeface = mediumTypeface
                val color = ColorCompat.getThemedColor(layout.context, com.lytefast.flexinput.R.b.colorInteractiveNormal)
                previewText.setTextColor(color)
                previewText.gravity = android.view.Gravity.CENTER_VERTICAL
                // Further increase top and bottom padding to prevent emoji cropping
                val extraPad = DimenUtils.dpToPx(16)
                previewText.setPadding(
                    previewText.paddingLeft,
                    previewText.paddingTop + extraPad,
                    previewText.paddingRight,
                    previewText.paddingBottom + extraPad
                )
                // After setting text, force layout and set minHeight to lineHeight
                previewText.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                    val lh = previewText.lineHeight
                    previewText.minHeight = lh
                }

                // Use Discord's message parser and AST renderer for mentions/emojis
                val draweeBuilder = DraweeSpanStringBuilder()
                val initialState = com.aliucord.utils.ReflectUtils.getField(
                    com.discord.utilities.textprocessing.MessageParseState::class.java,
                    null,
                    "initialState"
                ) as com.discord.utilities.textprocessing.MessageParseState
                val ctx = com.discord.utilities.textprocessing.MessageRenderContext(
                    layout.context,
                    channelId,
                    false,
                    mutableMapOf<Long, String>(),
                    mutableMapOf<Long, String>(),
                    mutableMapOf<Long, GuildRole>(),
                    0
                )
                val ast = com.aliucord.utils.MDUtils.parser.parse(messageContent ?: "", initialState)
                ast.forEach { it.render(draweeBuilder, ctx) }
                previewText.setText(draweeBuilder)
                previewText.setPadding(DimenUtils.dpToPx(16), DimenUtils.dpToPx(2), 0, 0)
                layout.addView(previewText, 1)

                // Extract attachments from intent (fix deprecation and unchecked cast warnings)
                @Suppress("DEPRECATION")
                val attachmentsList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra(EXTRA_ATTACHMENTS_INTENT, java.util.ArrayList::class.java) as? ArrayList<*>
                } else {
                    @Suppress("UNCHECKED_CAST")
                    intent.getSerializableExtra(EXTRA_ATTACHMENTS_INTENT) as? ArrayList<*>
                }
                val safeAttachmentsList: List<Map<String, String>>? = attachmentsList?.mapNotNull {
                    if (it is Map<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        it as? Map<String, String>
                    } else null
                }

                if (attachmentsList != null && attachmentsList.isNotEmpty()) {
                    // Add a horizontal LinearLayout for compact display
                    val attachmentsLayout = LinearLayout(layout.context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(DimenUtils.dpToPx(16), DimenUtils.dpToPx(4), 0, 0)
                    }
                    // Limit to 3 images for compactness
                    var imageCount = 0
                    safeAttachmentsList?.forEach { att ->
                        try {
                            val attObj = att as? ForwardedAttachment ?: return@forEach
                            val filename = attObj.filename
                            val url = attObj.url
                            val type = attObj.type
                            val size = attObj.size
                            if (type.contains("IMAGE", true) && imageCount < 3) {
                                val imageView = com.facebook.drawee.view.SimpleDraweeView(layout.context).apply {
                                    val imgSize = DimenUtils.dpToPx(48)
                                    layoutParams = LinearLayout.LayoutParams(imgSize, imgSize).apply {
                                        setMargins(DimenUtils.dpToPx(2), 0, DimenUtils.dpToPx(2), 0)
                                    }
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                }
                                com.discord.utilities.icon.IconUtils.setIcon(imageView, url)
                                com.discord.utilities.images.MGImages.setRoundingParams(imageView, 8f, false, null, null, null)
                                attachmentsLayout.addView(imageView)
                                imageCount++
                            } else if (!type.contains("IMAGE", true)) {
                                // File: show filename as clickable link, underline, themed color, and file size if available
                                val fileNameView = TextView(layout.context).apply {
                                    text = if (size > 0) "$filename ($size bytes)" else filename
                                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f)
                                    setTextColor(ColorCompat.getThemedColor(layout.context, com.lytefast.flexinput.R.b.colorInteractiveNormal))
                                    paint.isUnderlineText = true
                                    setPadding(DimenUtils.dpToPx(2), 0, DimenUtils.dpToPx(2), 0)
                                    setOnClickListener {
                                        Utils.openMediaViewer(url, filename)
                                    }
                                }
                                attachmentsLayout.addView(fileNameView)
                            }
                        } catch (_: Throwable) {}
                    }
                    // Insert attachments below previewText
                    layout.addView(attachmentsLayout, 2)
                }
            })

        // Send forwarded message
        patcher.patch(WidgetIncomingShare::class.java.getDeclaredMethod(
            "onSendClicked",
            Context::class.java,
            WidgetGlobalSearchModel.ItemDataPayload::class.java,
            ViewEmbedGameInvite.Model::class.java,
            WidgetIncomingShare.ContentModel::class.java,
            Boolean::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Boolean::class.javaPrimitiveType,
            CaptchaHelper.CaptchaPayload::class.java
        ), PreHook { param ->
            val share = param.thisObject as WidgetIncomingShare
            val itemDataPayload = param.args[1] as WidgetGlobalSearchModel.ItemDataPayload
            val intent = share.mostRecentIntent

            val binding: WidgetIncomingShareBinding = try {
                bindingReflection.invoke(share) as WidgetIncomingShareBinding
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

            val textInput = binding.d.editText

            val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID_INTENT, 0)
            val channelId = intent.getLongExtra(EXTRA_CHANNEL_ID_INTENT, 0)
            if (messageId != 0L && channelId != 0L) {
                val selectedChannel = itemDataPayload.channel.k()
                val commentMessage = textInput?.text?.toString() ?: ""
                Utils.threadPool.submit {
                    try {
                        val forwardMsg = Message(MessageReference(1, messageId, channelId, null, false), "")

                        val res = Http.Request
                            .newDiscordRNRequest("/channels/$selectedChannel/messages", "POST")
                            .executeWithJson(forwardMsg)

                        val respText = try { res.text() } catch (e: Exception) { "<unable to read body: ${e.message}>" }

                        if (!res.ok())
                            Toast.makeText(context, "Forwarding failed: ${res.statusCode}", Toast.LENGTH_SHORT).show()
                        else {
                            if (commentMessage.isNotEmpty()) {
                                val commentMsg = Message(null, commentMessage)

                                val cres = Http.Request.newDiscordRNRequest("/channels/$selectedChannel/messages", "POST")
                                    .executeWithJson(commentMsg)
                                val cresText = try { cres.text() } catch (e: Exception) { "<unable to read body: ${e.message}>" }
                            }

                            Utils.mainThread.post {
                                if (ForwardSettings.showToast) {
                                    Toast.makeText(context, "Message forwarded!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }

                Utils.mainThread.post {
                    share.startActivity(IntentUtils.RouteBuilders.selectChannel(selectedChannel, 0, null)
                        .setPackage(context.packageName))
                }

                param.result = null
            }
        })

        // Activate send button always if forwarding
        patcher.patch(WidgetIncomingShare::class.java.getDeclaredMethod("configureUi", WidgetIncomingShare.Model::class.java, Clock::class.java),
            PreHook { param ->
                val model = param.args[0] as WidgetIncomingShare.Model
                val share = param.thisObject as WidgetIncomingShare
                val intent = share.mostRecentIntent
                val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID_INTENT, 0)
                try {
                    if (messageId != 0L) {
                        modelCommentField.set(model, "...")
                    }
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            })
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }

    data class MessageReference(
        @com.aliucord.utils.SerializedName("type") val type: Int,
        @com.aliucord.utils.SerializedName("message_id") val messageId: Long,
        @com.aliucord.utils.SerializedName("channel_id") val channelId: Long,
        @com.aliucord.utils.SerializedName("guild_id") val guildId: Long?,
        @com.aliucord.utils.SerializedName("fail_if_not_exists") val failIfNotExists: Boolean
    )


    class Message(
        val message_reference: MessageReference?,
        content: String = "",
        flags: Int = 0,
        tts: Boolean = false,
        nonce: String = Utils.generateRNNonce().toString(),
        context: Context = Utils.appContext
    ) : com.aliucord.entities.RNMessage(content, flags, tts, nonce, context)
}

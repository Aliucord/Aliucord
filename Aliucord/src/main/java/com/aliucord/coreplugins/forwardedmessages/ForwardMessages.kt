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
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
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
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

internal class ForwardMessages : CorePlugin(Manifest("ForwardMessages")) {
    private val forwardExtraContent = "com.aliucord.coreplugins.forwardedmessages.EXTRA_CONTENT"
    private val forwardExtraMessageId = "com.aliucord.coreplugins.forwardedmessages.EXTRA_MESSAGE_ID"
    private val forwardExtraChannelId = "com.aliucord.coreplugins.forwardedmessages.EXTRA_CHANNEL_ID"

    init {
        settingsTab = SettingsTab(ForwardSettings.Sheet::class.java, SettingsTab.Type.BOTTOM_SHEET)
    }

    override fun start(context: Context) {
        val forwardId = View.generateViewId()

        val replyIcon: Drawable? = ContextCompat.getDrawable(Utils.appActivity, com.lytefast.flexinput.R.e.ic_reply_24dp)?.mutate()
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
                    val tw = TextView(lay.context, null, 0, com.lytefast.flexinput.R.i.UiKit_Settings_Item_Icon)
                    tw.id = forwardId
                    tw.text = "Forward"
                    val mediumTypeface = ResourcesCompat.getFont(tw.context, Constants.Fonts.whitney_medium)
                    if (mediumTypeface != null) tw.typeface = mediumTypeface
                    val color = ColorCompat.getThemedColor(tw.context, com.lytefast.flexinput.R.b.colorInteractiveNormal)
                    forwardIcon.setTintList(android.content.res.ColorStateList.valueOf(color))
                    tw.setCompoundDrawablesRelativeWithIntrinsicBounds(forwardIcon, null, null, null)

                    val childrenCount = lay.childCount
                    var foundIndex = false
                    for (i in 0 until childrenCount) {
                        val view = lay.getChildAt(i)
                        if (view.id == Utils.getResId("dialog_chat_actions_reply", "id")) {
                            foundIndex = true
                            lay.addView(tw, i + 1)
                            break
                        }
                    }
                    if (!foundIndex) lay.addView(tw, 5)

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
                                val wrapper = com.aliucord.wrappers.messages.AttachmentWrapper(msgAttachment)
                                val map = java.util.HashMap<String, String>()
                                map["filename"] = wrapper.filename ?: ""
                                map["url"] = wrapper.proxyUrl ?: wrapper.url ?: ""
                                map["type"] = wrapper.type?.name ?: ""
                                map
                            } else null
                        }
                        val putExtra = Intent()
                            .putExtra(forwardExtraContent, messageContent)
                            .putExtra(forwardExtraMessageId, messageId)
                            .putExtra(forwardExtraChannelId, channelId)
                            .putExtra("forwarded_attachments", attachmentInfo?.let { ArrayList(it) } ?: ArrayList<HashMap<String, String>>())

                        Utils.mainThread.post {
                            Utils.openPage(Utils.appActivity, WidgetIncomingShare::class.java, putExtra)
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
                val messageId = intent.getLongExtra(forwardExtraMessageId, 0)
                val channelId = intent.getLongExtra(forwardExtraChannelId, 0)
                val messageContent = intent.getStringExtra(forwardExtraContent)

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
                val draweeBuilder = Class.forName("com.facebook.drawee.span.DraweeSpanStringBuilder").getConstructor().newInstance() as android.text.SpannableStringBuilder
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

                // Extract attachments from intent
                val attachmentsList = intent.getSerializableExtra("forwarded_attachments") as? ArrayList<Map<String, String>>?
                com.aliucord.Logger("ForwardMessages").info("Preview attachmentsList from intent: $attachmentsList")

                if (attachmentsList != null && attachmentsList.isNotEmpty()) {
                                    com.aliucord.Logger("ForwardMessages").info("attachmentsList is ${attachmentsList?.size} items: $attachmentsList")
                    // Add a horizontal LinearLayout for compact display
                    val attachmentsLayout = LinearLayout(layout.context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(DimenUtils.dpToPx(16), DimenUtils.dpToPx(4), 0, 0)
                    }
                    // Limit to 3 images for compactness
                    var imageCount = 0
                    attachmentsList.forEach { att ->
                        try {
                            val filename = att["filename"] ?: ""
                            val url = att["url"] ?: ""
                            val type = att["type"] ?: ""
                            com.aliucord.Logger("ForwardMessages").info("Attachment: $filename, type: $type, url: $url")
                            if (type.contains("IMAGE", true) && imageCount < 3) {
                                val imageView = com.facebook.drawee.view.SimpleDraweeView(layout.context).apply {
                                    val size = DimenUtils.dpToPx(48)
                                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                                        setMargins(DimenUtils.dpToPx(2), 0, DimenUtils.dpToPx(2), 0)
                                    }
                                    scaleType = ImageView.ScaleType.CENTER_CROP
                                }
                                com.aliucord.Logger("ForwardMessages").info("Loading image: $url")
                                com.discord.utilities.icon.IconUtils.setIcon(imageView, url)
                                com.discord.utilities.images.MGImages.setRoundingParams(imageView, 8f, false, null, null, null)
                                attachmentsLayout.addView(imageView)
                                imageCount++
                            } else if (!type.contains("IMAGE", true)) {
                                // File: show filename and download icon
                                val fileLayout = LinearLayout(layout.context).apply {
                                    orientation = LinearLayout.VERTICAL
                                    setPadding(DimenUtils.dpToPx(2), 0, DimenUtils.dpToPx(2), 0)
                                }
                                val fileNameView = TextView(layout.context).apply {
                                    text = filename
                                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 10f)
                                    setTextColor(ColorCompat.getThemedColor(layout.context, com.lytefast.flexinput.R.b.colorTextMuted))
                                }
                                val downloadIcon = ImageView(layout.context).apply {
                                    setImageResource(android.R.drawable.stat_sys_download)
                                    setOnClickListener {
                                        Utils.openMediaViewer(url, filename)
                                    }
                                    val size = DimenUtils.dpToPx(24)
                                    layoutParams = LinearLayout.LayoutParams(size, size)
                                }
                                fileLayout.addView(fileNameView)
                                fileLayout.addView(downloadIcon)
                                attachmentsLayout.addView(fileLayout)
                            }
                        } catch (e: Throwable) {
                            com.aliucord.Logger("ForwardMessages").error("Attachment render error", e)
                        }
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

            val messageId = intent.getLongExtra(forwardExtraMessageId, 0)
            val channelId = intent.getLongExtra(forwardExtraChannelId, 0)
            if (messageId != 0L && channelId != 0L) {
                val selectedChannel = itemDataPayload.channel.k()
                val commentMessage = textInput?.text?.toString() ?: ""
                Utils.threadPool.submit {
                    try {
                        val forwardMsg = Message(MessageReference(1, messageId, channelId, null, false), "")

                        val res = Http.Request
                            .newDiscordRNRequest(String.format("/channels/%d/messages", selectedChannel), "POST")
                            .executeWithJson(forwardMsg)

                        val respText = try { res.text() } catch (e: Exception) { "<unable to read body: ${e.message}>" }

                        if (!res.ok())
                            Toast.makeText(context, "Forwarding failed: ${res.statusCode}", Toast.LENGTH_SHORT).show()
                        else {
                            if (commentMessage.isNotEmpty()) {
                                val commentMsg = Message(null, commentMessage)

                                val cres = Http.Request.newDiscordRNRequest(String.format("/channels/%d/messages", selectedChannel), "POST")
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
                val messageId = intent.getLongExtra(forwardExtraMessageId, 0)
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

    class MessageReference(
        @JvmField var type: Int,
        @JvmField var message_id: Long,
        @JvmField var channel_id: Long,
        @JvmField var guild_id: Long?,
        @JvmField var fail_if_not_exists: Boolean
    )

    fun nextBits(rng: Random, bits: Int): Int {
        if (bits < 0 || bits > 32) throw IllegalArgumentException("bits must be 0..32")
        if (bits == 0) return 0
        if (bits == 32) return rng.nextInt()
        val mask = (1 shl bits) - 1
        return rng.nextInt() and mask
    }

    fun nextBits(bits: Int): Int = nextBits(ThreadLocalRandom.current(), bits)

    class Message(
        @JvmField var message_reference: MessageReference?,
        @JvmField var content: String = ""
    ) {
        @JvmField var flags: Int = 0
        @JvmField var tts: Boolean = false
        @JvmField var nonce: String = (SnowflakeUtils.fromTimestamp(System.currentTimeMillis()) + (ThreadLocalRandom.current().nextInt() and ((1 shl 23) - 1))).toString()
        @JvmField var mobile_network_type: String = "unknown"
        @JvmField var signal_strength: Int = 0

        init {
            val context = Utils.appActivity.applicationContext
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val activeNetwork: Network? = connectivityManager.activeNetwork
            if (activeNetwork != null) {
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (capabilities != null) {
                    mobile_network_type = when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                        else -> mobile_network_type
                    }

                    if (Build.VERSION.SDK_INT >= 28) {
                        val ss: SignalStrength? = telephonyManager.signalStrength
                        signal_strength = ss?.level ?: 0
                    }
                }
            }
        }
    }
}

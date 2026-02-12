package com.aliucord.coreplugins.channelbrowser

import android.annotation.SuppressLint
import com.aliucord.Http
import android.view.Gravity
import android.view.View
import android.content.Context
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.ContextCompat
import com.aliucord.Constants
import com.aliucord.api.SettingsAPI
import com.lytefast.flexinput.R
import com.aliucord.fragments.SettingsPage
import com.discord.stores.StoreStream

internal class ChannelBrowserPage(val settings: SettingsAPI) : SettingsPage() {

    private val logger = com.aliucord.Logger("ChannelBrowser")
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var lastView: View? = null

    private fun getCurrentGuildSettings(guildId: Long): Map<String, Any>? = try {
        val store = StoreStream.getUserGuildSettings()
        val settingsMap = store.guildSettings
        val settings = settingsMap[guildId] ?: return null
        val field = settings.javaClass.declaredFields.find { it.name == "channelOverrides" }
        field?.isAccessible = true
        val overridesList = field?.get(settings) as? List<*> ?: return null
        val overridesMap = mutableMapOf<String, Int>()
        for (override in overridesList) {
            if (override == null) continue
            val chIdField = override.javaClass.declaredFields.find { it.name == "channelId" }
            val flagsField = override.javaClass.declaredFields.find { it.name == "flags" }
            chIdField?.isAccessible = true
            flagsField?.isAccessible = true
            val chId = chIdField?.get(override)?.toString()
            val flags = (flagsField?.get(override) as? Int) ?: 0
            if (chId != null) overridesMap[chId] = flags
        }
        mapOf("channel_overrides" to overridesMap)
    } catch (e: Throwable) {
        logger.error("[getCurrentGuildSettings] Exception: ${e.message}", e)
        null
    }

    private fun themeAlertDialogText(dialog: AlertDialog, ctx: Context) {
        try {
            val textColor = ContextCompat.getColor(ctx, R.c.primary_dark)
            dialog.window?.decorView?.post {
                val messageView = dialog.findViewById<TextView>(android.R.id.message)
                messageView?.setTextColor(textColor)
                messageView?.typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_medium)
            }
        } catch (_: Throwable) {}
    }

    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View) {
        lastView = view
        super.onViewBound(view)

        setActionBarTitle("Browse Channels")
        setActionBarSubtitle(null)

        val ctx = context ?: return
        val guildId = StoreStream.getGuildSelected().selectedGuildId
        val allChannelsRaw = StoreStream.getChannels().getChannelsForGuild(guildId)
        val hiddenChannels = settings.getObject("hiddenChannels", mutableListOf<String>()) as MutableList<String>

        val channelOverridesMap = (getCurrentGuildSettings(guildId)?.get("channel_overrides") as? Map<*, *>)
            ?.filter { it.key is String && it.value is Int }
            ?.map { it.key as String to it.value as Int }
            ?.toMap() ?: emptyMap()

        try {
            val store = StoreStream.getUserGuildSettings()
            com.discord.utilities.rx.ObservableExtensionsKt.appSubscribe(
                store.observeGuildSettings(guildId),
                ChannelBrowserPage::class.java,
                ctx,
                {},
                { _: com.discord.utilities.error.Error -> },
                {},
                {},
                { _: Any? -> }
            )
        } catch (e: Throwable) {
            logger.error("[observeGuildSettings] Exception", e)
        }

        val typeField = com.discord.api.channel.Channel::class.java.getDeclaredField("type").apply { isAccessible = true }
        val parentIdField = com.discord.api.channel.Channel::class.java.getDeclaredField("parentId").apply { isAccessible = true }
        val idField = com.discord.api.channel.Channel::class.java.getDeclaredField("id").apply { isAccessible = true }
        val nameField = com.discord.api.channel.Channel::class.java.getDeclaredField("name").apply { isAccessible = true }

        val linearLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        addView(linearLayout)

        val categories = allChannelsRaw.values.filter {
            try {
                typeField.getInt(it) == 4
            } catch (_: Throwable) {
                false
            }
        }

        val channelsByCategory = mutableMapOf<Long, MutableList<com.discord.api.channel.Channel>>()
        val uncategorized = mutableListOf<com.discord.api.channel.Channel>()

        for (ch in allChannelsRaw.values) {
            val type = try { typeField.getInt(ch) } catch (_: Throwable) { -1 }
            if (type == 4) continue
            val parentId = try { parentIdField.get(ch) as? Long } catch (_: Throwable) { null }
            if (parentId != null && allChannelsRaw.containsKey(parentId)) {
                channelsByCategory.getOrPut(parentId) { mutableListOf() }.add(ch)
            } else {
                uncategorized.add(ch)
            }
        }

        for (cat in categories) {
            val catName = try { nameField.get(cat) as? String ?: "Unnamed Category" } catch (_: Throwable) { "Unnamed Category" }
            val catId = try { idField.get(cat) as? Long } catch (_: Throwable) { null }

            val catRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 24, 0, 8)
                gravity = Gravity.CENTER_VERTICAL
            }

            val catTv = TextView(ctx, null, 0, R.i.UiKit_Settings_Item).apply {
                text = catName
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_bold)
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val followLabel = TextView(ctx, null, 0, R.i.UiKit_Settings_Item).apply {
                text = "Follow Category"
                val color = try {
                    com.discord.utilities.color.ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal)
                } catch (_: Throwable) {
                    0xFF222222.toInt()
                }
                setTextColor(color)
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_medium)
                textSize = 14f
                setPadding(16, 0, 16, 0)
            }

            val children = catId?.let { channelsByCategory[it] }
            val childIds = children?.mapNotNull { ch ->
                try { ch.javaClass.getDeclaredField("id").apply { isAccessible = true }.get(ch)?.toString() } catch (_: Throwable) { null }
            } ?: emptyList()

            val isCategoryHiddenLocally = catId != null && hiddenChannels.contains(catId.toString())
            val checkedCount = childIds.count { id -> (channelOverridesMap[id] ?: 4096) and 4096 != 0 }
            val allChecked = checkedCount == childIds.size && childIds.isNotEmpty()

            val catToggle = Switch(ctx).apply {
                isChecked = !isCategoryHiddenLocally && allChecked
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { gravity = Gravity.CENTER_VERTICAL }
            }

            catToggle.setOnCheckedChangeListener { _, checked ->
                if (catId != null) {
                    catToggle.isEnabled = false
                    catToggle.isChecked = checked
                    Thread {
                        val newOverridesMap = mutableMapOf<String, MutableMap<String, Any>>()
                        newOverridesMap[catId.toString()] =
                            mutableMapOf("channel_id" to catId.toString(), "flags" to if (checked) 4096 else 0)
                        val localHidden =
                            settings.getObject("hiddenChannels", mutableListOf<String>()) as MutableList<String>
                        val prevHiddenKey = "catPrevHidden_$catId"
                        if (checked) {
                            val prevHidden = childIds.filter { localHidden.contains(it) }
                            settings.setObject(prevHiddenKey, prevHidden)
                            for (chId in childIds) {
                                localHidden.remove(chId)
                            }
                            localHidden.remove(catId.toString())
                        } else {
                            val prevHidden =
                                settings.getObject(prevHiddenKey, mutableListOf<String>()) as MutableList<String>
                            for (chId in childIds) {
                                if (prevHidden.contains(chId)) {
                                    if (!localHidden.contains(chId)) localHidden.add(chId)
                                } else {
                                    localHidden.remove(chId)
                                }
                            }
                            if (!localHidden.contains(catId.toString())) localHidden.add(catId.toString())
                        }
                        settings.setObject("hiddenChannels", localHidden)
                        val syncToPC = settings.getBool("syncToPC", true)
                        if (syncToPC) {
                            val patchBody = mapOf(
                                "guilds" to mapOf(
                                    guildId.toString() to mapOf(
                                        "channel_overrides" to newOverridesMap
                                    )
                                )
                            )
                            try {
                                val req = Http.Request.newDiscordRNRequest(
                                    "/users/@me/guilds/settings",
                                    "PATCH"
                                )
                                val resp = req.executeWithJson(patchBody)
                            } catch (_: Exception) {
                            }
                        }
                        lastView?.let { v ->
                            if (syncToPC) {
                                handler.post {
                                    catToggle.isEnabled = true
                                    onViewBound(v)
                                }
                            } else {
                                handler.postDelayed({
                                    catToggle.isEnabled = true
                                    onViewBound(v)
                                }, 250)
                            }
                        }
                    }.start()
                }
            }
            catRow.addView(catTv)
            catRow.addView(followLabel)
            catRow.addView(catToggle)
            linearLayout.addView(catRow)

            if (children != null) {
                val isCategoryFollowed = catToggle.isChecked
                for (ch in children) {
                    addChannelRowReflect(
                        ch, guildId, ctx, nameField,
                        channelOverridesMap, linearLayout, false, isCategoryFollowed, hiddenChannels
                    )
                }
            }
        }

        if (uncategorized.isNotEmpty()) {
            TextView(ctx, null, 0, R.i.UiKit_Settings_Item).apply {
                text = "Uncategorized"
                typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_bold)
                textSize = 15f
                setPadding(0, 24, 0, 8)
            }.let { linearLayout.addView(it) }

            for (ch in uncategorized) {
                addChannelRowReflect(
                    ch,
                    guildId,
                    ctx,
                    nameField,
                    channelOverridesMap,
                    linearLayout,
                    grayOut = false,
                    parentCategoryHidden = false,
                    hiddenChannels = hiddenChannels
                )
            }
        }
    }

    private fun addChannelRowReflect(
        ch: com.discord.api.channel.Channel,
        guildId: Long,
        ctx: Context,
        nameField: java.lang.reflect.Field,
        channelOverridesMap: Map<String, Int>,
        linearLayout: LinearLayout,
        grayOut: Boolean = false,
        parentCategoryHidden: Boolean = false,
        hiddenChannels: MutableList<String>
    ) {
        val chName = try {
            nameField.get(ch) as? String ?: "Unnamed Channel"
        } catch (_: Throwable) {
            "Unnamed Channel"
        }
        val chId = try {
            ch.javaClass.getDeclaredField("id").apply { isAccessible = true }.get(ch)?.toString()
        } catch (_: Throwable) {
            null
        }
        val type = try { ch.javaClass.getDeclaredField("type").apply { isAccessible = true }.getInt(ch) } catch (_: Throwable) { -1 }
        val flags = if (chId != null) channelOverridesMap[chId] ?: 4096 else 4096
        val isHiddenLocally = hiddenChannels.contains(chId)
        val isCheckedRemote = (flags and 4096) != 0
        val isChecked = !isHiddenLocally && isCheckedRemote
        val suppressChannelListener = BooleanArray(1) { false }

        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 8, 16, 8)
            gravity = Gravity.CENTER_VERTICAL
        }

        val iconView = ImageView(ctx).apply {
            try {
                val iconRes = when (type) {
                    2 -> R.e.ic_channel_voice 
                    else -> R.e.ic_channel_text 
                }
                val drawable = ctx.getDrawable(iconRes)?.mutate()
                try {
                    val color = com.discord.utilities.color.ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal)
                    drawable?.setTint(color)
                } catch (_: Throwable) {}
                setImageDrawable(drawable)
                val scale = ctx.resources.displayMetrics.density
                val size = (20 * scale).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(0, 0, (8 * scale).toInt(), 0)
                    gravity = Gravity.CENTER_VERTICAL
                }
            } catch (_: Throwable) {
                layoutParams = LinearLayout.LayoutParams(0, 0)
            }
        }
        val tv = TextView(ctx, null, 0, R.i.UiKit_Settings_Item_SubText).apply {
            text = chName
            typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_medium)
            textSize = 14f
            val color = try {
                if (isChecked) {
                    com.discord.utilities.color.ColorCompat.getThemedColor(ctx, R.b.colorInteractiveMuted)
                } else {
                    com.discord.utilities.color.ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal)
                }
            } catch (_: Throwable) {
                if (isChecked) 0xFF222222.toInt() else 0xFF888888.toInt()
            }
            setTextColor(color)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val cb = CheckBox(ctx)
        cb.isChecked = isChecked
        cb.isEnabled = !parentCategoryHidden
        cb.layoutParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
        cb.setOnCheckedChangeListener { buttonView, checked ->
            if (suppressChannelListener[0]) return@setOnCheckedChangeListener
            val previousState = !checked
            cb.isChecked = checked
            row.alpha = if (!checked || parentCategoryHidden) 0.5f else 1.0f
            val doAction = {
                if (chId != null) {
                    suppressChannelListener[0] = true
                    Thread {
                        val channelOverrides = mutableMapOf<String, MutableMap<String, Any>>()
                        val toggledOverride =
                            mutableMapOf<String, Any>("channel_id" to chId, "flags" to if (checked) 4096 else 0)
                        channelOverrides[chId] = toggledOverride
                        if (!checked) {
                            if (!hiddenChannels.contains(chId)) hiddenChannels.add(chId)
                        } else {
                            hiddenChannels.remove(chId)
                        }
                        settings.setObject("hiddenChannels", hiddenChannels)
                        if (settings.getBool("syncToPC", true)) {
                            val patchBody = mapOf(
                                "guilds" to mapOf(
                                    guildId.toString() to mapOf(
                                        "channel_overrides" to channelOverrides
                                    )
                                )
                            )
                            try {
                                val req = Http.Request.newDiscordRNRequest(
                                    "/users/@me/guilds/settings",
                                    "PATCH"
                                )
                                val resp = req.executeWithJson(patchBody)
                            } catch (_: Exception) {
                            }
                        }
                        handler.post {
                            suppressChannelListener[0] = false
                        }
                    }.start()
                }
            }
            if (settings.getBool("confirmActions", false)) {
                val textColor = ContextCompat.getColor(ctx, R.c.primary_dark)
                val customTitle = TextView(ctx).apply {
                    text = if (!checked) "Hide Channel" else "Restore Channel"
                    setTextColor(textColor)
                    typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_bold)
                    textSize = 20f
                    setPadding(32, 32, 32, 16)
                }
                val themedDialog = AlertDialog.Builder(ctx)
                    .setCustomTitle(customTitle)
                    .setMessage("Are you sure you want to ${if (!checked) "hide" else "restore"} this channel?")
                    .setPositiveButton("Yes") { _: android.content.DialogInterface, _: Int -> doAction() }
                    .setNegativeButton("No") { _: android.content.DialogInterface, _ ->
                        suppressChannelListener[0] = true
                        buttonView.isChecked = previousState
                        suppressChannelListener[0] = false
                    }
                    .setOnCancelListener {
                        buttonView.isChecked = previousState
                    }
                    .create()
                themedDialog.show()
                themeAlertDialogText(themedDialog, ctx)
            } else {
                doAction()
            }
        }
        row.addView(iconView)
        row.addView(tv)
        row.addView(cb)
        row.alpha = if (!isChecked || grayOut) 0.5f else 1.0f
        linearLayout.addView(row)
    }
}



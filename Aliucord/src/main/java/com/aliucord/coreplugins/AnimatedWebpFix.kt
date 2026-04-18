package com.aliucord.coreplugins

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.api.message.embed.EmbedType
import com.discord.utilities.embed.EmbedResourceUtils
import com.discord.utilities.icon.IconUtils
import com.discord.utilities.string.StringUtilsKt
import com.discord.widgets.chat.list.InlineMediaView
import de.robv.android.xposed.XposedBridge

@SuppressLint("UseKtx")
internal class AnimatedWebpFix: CorePlugin(Manifest("AnimatedWebpFix")) {

    init {
        manifest.description = "Fixes animated emojis and avatars by adding animated webp support."
    }

    val animatedRegex = Regex("[?&]animated=true")

    override fun start(context: Context) {
        // deoptimize overloads and remove inlining
        val methodsToDeoptimize = mapOf(
            IconUtils::class.java to "getForUser",
            IconUtils::class.java to "getForGuildMember",
            InlineMediaView::class.java to "shouldAutoPlay",
            InlineMediaView::class.java to "updateUI"
        )
        methodsToDeoptimize.forEach { (klass, string) ->
            val methods = klass.declaredMethods.filter { it.name.contains(string) }

            methods.forEach(XposedBridge::deoptimizeMethod)
        }

        patcher.before<EmbedResourceUtils>("isAnimated",
            EmbedType::class.java,
            String::class.java
        ) { (param, _: EmbedType?, str: String?)  ->

            val uri = Uri.parse(str) ?: return@before
            val path = uri.path ?: return@before
            val isWebp = path.endsWith(".webp")
            // proxy url queries are impossible to access via Uri class
            val isWebpAnimated = path.contains(animatedRegex)
            if (isWebpAnimated && isWebp) {
                // might seem counterintuitive but trust
                param.result = false
            }
        }

        // add animated query to emoji url
        patcher.after<EmbedResourceUtils>(
            "getPreviewUrls",
            String::class.java, Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!
        ) { (param, _: String, _: Int, _: Int, animated: Boolean) ->
            if (!animated) return@after

            @Suppress("UNCHECKED_CAST")
            val urls = (param.result as List<String>).toMutableList()
            val uri = Uri.parse(urls[0])

            val isWebp = uri.path?.endsWith(".webp") == true

            val newUri = uri.buildUpon()
                .apply { if (isWebp) appendQueryParameter("animated", "true") }
                .build()

            urls[0] = newUri.toString()
            param.result = urls
        }

        // force animated icons to be webp
        patcher.instead<IconUtils>("getImageExtension",
            String::class.java,
            Boolean::class.javaPrimitiveType!!
        ) { (_, hash: String, animated: Boolean) ->
            if (animated && isImageHashAnimated(hash)) {
                "webp"
            } else {
                StringUtilsKt.getSTATIC_IMAGE_EXTENSION()
            }
        }

        // add animated query for user and guild member avatar
        patcher.after<IconUtils?>("getForUser",
            Long::class.javaObjectType,
            String::class.java,
            Int::class.javaObjectType,
            Boolean::class.javaPrimitiveType!!,
            Int::class.javaObjectType
        ) { (param, _: Long, _: String, _: Int, animated: Boolean, _: Int) ->
            if (animated) {
                val result = param.result as? String ?: return@after
                val newUri = Uri.parse(result)
                    .buildUpon()
                    .appendQueryParameter("animated", "true")
                    .build()

                param.result = newUri.toString()
            }
        }


        patcher.after<IconUtils>("getForGuildMember",
            String::class.java,
            Long::class.javaPrimitiveType!!,
            Long::class.javaPrimitiveType!!,
            Int::class.javaObjectType,
            Boolean::class.javaPrimitiveType!!
        ) { (param, _: String, _: Long, _: Long, _: Int, animated: Boolean) ->
            if (animated) {
                val result = param.result as? String ?: return@after
                val newUri = Uri.parse(result)
                    .buildUpon()
                    .appendQueryParameter("animated", "true")
                    .build()

                param.result = newUri.toString()
            }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}

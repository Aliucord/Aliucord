package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.before
import com.linecorp.apng.decoder.Apng

internal class StickerCrashFix : CorePlugin(Manifest("StickerCrashFix")) {
    override val isHidden = true
    override val isRequired = true

    override fun load(context: Context) {
        patcher.before<Apng>(
            Integer::class.javaPrimitiveType!!,
            Integer::class.javaPrimitiveType!!,
            Integer::class.javaPrimitiveType!!,
            Integer::class.javaPrimitiveType!!,
            IntArray::class.java,
            Integer::class.javaPrimitiveType!!,
            Long::class.javaPrimitiveType!!,
        ) { param ->
            val durations = param.args[4] as IntArray
            param.args[4] = durations.map { if (it <= 10) 100 else it }.toIntArray()
        }
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}
}

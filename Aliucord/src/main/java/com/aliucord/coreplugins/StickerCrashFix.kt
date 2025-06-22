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
            for ((index, duration) in durations.withIndex())
                if (duration <= 10)
                    durations[index] = 100
        }
    }

    override fun start(context: Context) {}
    override fun stop(context: Context) {}
}

package com.aliucord.coreplugins

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.before
import de.robv.android.xposed.XC_MethodHook.MethodHookParam

internal class OpenLinksExternallyFix : CorePlugin(Manifest("OpenLinksExternallyFix")) {
    override val isHidden = true

    init {
        manifest.description = "Forces app links to always open in a separate window, except for custom tab. " +
            "This addresses an issue where some links are opened internally on certain ROMs"
    }

    override fun start(context: Context) {
        fun handleIntent(param: MethodHookParam) {
            val intent = param.args[0] as? Intent ?: return
            val pm = context.packageManager
            val handlers = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (handlers.isEmpty()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        patcher.before<Activity>("startActivity", Intent::class.java) { param -> handleIntent(param) }
        patcher.before<Activity>("startActivityForResult", Intent::class.java, Int::class.javaPrimitiveType!!, Bundle::class.java) { param -> handleIntent(param) }
    }
    override fun stop(context: Context) {}
}
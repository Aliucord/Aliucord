package com.aliucord.coreplugins

import android.content.Context
import android.widget.TextView
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.api.auth.OAuthScope
import com.discord.views.OAuthPermissionViews

internal class AuthorizedAppsFix : CorePlugin(Manifest("AuthorizedAppsFix")) {
    override val isHidden = true

    init {
        manifest.description = "Fixes the Authorized apps page crashing."
    }

    override fun start(context: Context) {
        Patcher.addPatch(
            OAuthPermissionViews::class.java.getMethod(
                "a",
                TextView::class.java,
                OAuthScope::class.java
            ),
            Hook {
                if (it.hasThrowable()) {
                    val exc = it.throwable
                    if (exc is OAuthPermissionViews.InvalidScopeException) {
                        val scope = exc.a()
                        // Label the scopes that aren't recognized by Discord
                        when (scope) {
                            "role_connections.write" -> (it.args[0] as TextView).text = "Update your connection and metadata for this application"
                            // Some scopes are expanded to multiple scopes internally, so you can't really determine whether the user has each of one of these scopes or not..
                            "sdk.social_layer" -> (it.args[0] as TextView).text = "This scope expands to multiple scopes internally :| ($scope)"
                            "sdk.social_layer_presence" -> (it.args[0] as TextView).text = "This scope expands to multiple scopes internally :| ($scope)"
                            else -> (it.args[0] as TextView).text = "Scope not recognized :( ($scope)"
                        }
                        it.throwable = null
                    }
                }
            }
        )
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}

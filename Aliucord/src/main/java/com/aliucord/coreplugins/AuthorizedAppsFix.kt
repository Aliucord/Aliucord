package com.aliucord.coreplugins

import android.content.Context
import android.widget.TextView
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.api.auth.OAuthScope
import com.discord.views.OAuthPermissionViews

internal class AuthorizedAppsFix : CorePlugin(Manifest("AuthorizedAppsFix")) {
    override val isHidden = true
    override val isRequired = true

    init {
        manifest.description = "Fixes the Authorized apps page crashing."
    }

    override fun start(context: Context) {
        patcher.patch(OAuthPermissionViews::class.java.getMethod("a", TextView::class.java, OAuthScope::class.java), PreHook { (param, view: TextView, scope: OAuthScope) ->
            if (scope !is OAuthScope.Invalid) return@PreHook
            param.throwable = null
            when (val unrecognized_scope = scope.b()) {
                "role_connections.write" -> view.text = "Update your connection and metadata for this application"
                // Some scopes are expanded to multiple scopes internally, so you can't really determine whether the user has each of one of these scopes or not..
                "sdk.social_layer" -> view.text = "This scope expands to multiple scopes internally ($scope)"
                "sdk.social_layer_presence" -> view.text = "This scope expands to multiple scopes internally ($scope)"
                else -> view.text = "Scope not recognized ($scope)"
            }
        })
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}

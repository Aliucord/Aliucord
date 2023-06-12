/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins.rn

import com.aliucord.api.rn.user.RNUserProfile
import com.aliucord.patcher.*
import com.aliucord.utils.RNSuperProperties
import com.discord.api.user.UserProfile
import com.google.gson.reflect.TypeToken
import okhttp3.*
import rx.Observable
import java.lang.reflect.Type
import java.util.Collections
import java.util.TimeZone

class RNHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.c()
        val headersBuilder = req.d.e()
        headersBuilder.d("X-Super-Properties")
        headersBuilder.a("X-Super-Properties", RNSuperProperties.superPropertiesBase64)
        headersBuilder.d("User-Agent")
        headersBuilder.a("User-Agent", RNSuperProperties.userAgent)
        headersBuilder.a("X-Discord-Timezone", TimeZone.getDefault().id)
        return chain.a(Request(req.b, req.c, headersBuilder.c(), req.e, req.f))
    }
}

@Suppress("UNCHECKED_CAST")
fun patchUserProfile() {
    val original = TypeToken.getParameterized(Observable::class.java, UserProfile::class.java).type
    val new = TypeToken.getParameterized(Observable::class.java, RNUserProfile::class.java).type
    Patcher.addPatch(i0.y::class.java.getDeclaredMethod("a", Type::class.java, Array<Annotation>::class.java), PreHook {
        if (it.args[0] == original) it.args[0] = new
    })

    val interceptor = RNHeadersInterceptor()
    Patcher.addPatch(f0.e0.h.g::class.java.declaredConstructors[0], PreHook {
        if (it.args[2] != 0) return@PreHook
        val req = it.args[4] as Request
        if (req.b.i.last() == "profile") {
            val interceptors = it.args[1] as MutableList<Interceptor>
            interceptors.add(2, interceptor)
        }
    })

    /** discord doesn't check in [com.discord.widgets.user.WidgetUserMutualGuilds.Model] if mutualGuilds list is null */
    Patcher.addPatch(UserProfile::class.java.getDeclaredMethod("d"), Hook {
        if (it.result == null) it.result = Collections.EMPTY_LIST
    })
}

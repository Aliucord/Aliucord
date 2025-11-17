package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.rtcconnection.socket.io.Payloads.Protocol.ProtocolInfo

internal class VoiceFix : CorePlugin(Manifest("VoiceFix"))  {
    override val isHidden = true
    override val isRequired = true

    init {
        manifest.description = "Fixes VC not working properly."
    }

    override fun start(context: Context) {
        patcher.before<ProtocolInfo>(
            String::class.java,
            Int::class.javaPrimitiveType!!,
            String::class.java,
        ) { (param, _: String, _: Int, mode: String) ->
            if (mode == "xsalsa20_poly1305") {
                param.args[2] = "xsalsa20_poly1305_lite_rtpsize"
            }
        }
    }

    override fun stop(context: Context) = patcher.unpatchAll()
}


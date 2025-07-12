package com.aliucord.entities

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import com.aliucord.Utils

/**
 * A base for body of new Message requests, providing connection analytics and nonce that
 * are sent in RNA and Desktop.
 */
@SuppressLint("MissingPermission")
@Suppress("PrivatePropertyName", "unused")
abstract class RNMessage(context: Context = Utils.appContext) {
    protected open val content = ""
    protected open val flags = 0
    protected open val tts = false
    private var mobile_network_type = "unknown"
    private var signal_strength = 0
    private val nonce = Utils.generateRNNonce()

    init {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        connectivityManager.activeNetwork
            ?.let { connectivityManager.getNetworkCapabilities(it) }
            ?.let { capabilities ->
                mobile_network_type = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                    else -> "unknown"
                }
                if (Build.VERSION.SDK_INT >= 28) {
                    signal_strength = telephonyManager.signalStrength?.level ?: 0
                }
            }
    }
}

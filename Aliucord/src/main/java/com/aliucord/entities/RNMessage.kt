package com.aliucord.entities

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import com.aliucord.Utils
import com.aliucord.utils.SerializedName

/**
 * A base for body of new Message requests, providing connection analytics and nonce that
 * are sent in RNA and Desktop.
 */
@SuppressLint("MissingPermission")
@Suppress("PrivatePropertyName", "unused")
abstract class RNMessage(
    protected val content: String = "",
    protected val flags: Int = 0,
    protected val tts: Boolean = false,
    protected val nonce: String = Utils.generateRNNonce().toString(),
    context: Context = Utils.appContext,
) {
    @SerializedName("mobile_network_type")
    private var mobileNetworkType = "unknown"
    @SerializedName("signal_strength")
    private var signalStrength = 0

    init {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        connectivityManager.activeNetwork
            ?.let { connectivityManager.getNetworkCapabilities(it) }
            ?.let { capabilities ->
                mobileNetworkType = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                    else -> "unknown"
                }
                if (Build.VERSION.SDK_INT >= 28) {
                    signalStrength = telephonyManager.signalStrength?.level ?: 0
                }
            }
    }
}

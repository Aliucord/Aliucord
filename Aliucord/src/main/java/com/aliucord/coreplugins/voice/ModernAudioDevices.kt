package com.aliucord.coreplugins.voice

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.aliucord.Logger
import com.aliucord.api.PatcherAPI
import com.aliucord.patcher.after
import com.aliucord.patcher.before
import com.aliucord.patcher.component1
import com.aliucord.patcher.component2
import com.discord.rtcconnection.audio.DiscordAudioManager
import com.discord.rtcconnection.audio.DiscordAudioManager.AudioDevice
import com.discord.rtcconnection.audio.DiscordAudioManager.DeviceTypes
import kotlin.collections.joinToString

// Base aliucord's DiscordAudioManager is missing USB-C headsets
// DACs and LE Audio buds, so we route it to the speaker.
// Track modern outputs with AudioDeviceCallback and route LE audio
// via setCommunicationDevice
internal object ModernAudioDevices {
    private val logger = Logger("VoiceChatFix")

    private val USB_TYPES = run {
        var types = intArrayOf(
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_ACCESSORY,
            AudioDeviceInfo.TYPE_DOCK,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) types += AudioDeviceInfo.TYPE_USB_HEADSET
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) types += AudioDeviceInfo.TYPE_DOCK_ANALOG
        types
    }

    private val ANALOG_TYPES = intArrayOf(
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_LINE_ANALOG,
        AudioDeviceInfo.TYPE_AUX_LINE,
    )

    private val MODERN_BT_TYPES = run {
        var types = intArrayOf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            types += AudioDeviceInfo.TYPE_BLE_HEADSET
            types += AudioDeviceInfo.TYPE_BLE_SPEAKER
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) types += AudioDeviceInfo.TYPE_HEARING_AID
        types
    }

    @Volatile
    private var forcedBtAvailable = false

    fun AudioDeviceInfo.formatDevName(): String =
        "${this.productName}${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) " (${this.address})" else ""}"

    fun Array<out AudioDeviceInfo>.formatDevList(): String =
        this.joinToString { "${it.formatDevName()} type=${it.type} sink=${it.isSink}" }

    fun register(patcher: PatcherAPI) {
        logger.debug("ModernAudioDevices: register")

        patcher.after<DiscordAudioManager>(Context::class.java) audioManager@{
            this@audioManager.e.registerAudioDeviceCallback(object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(added: Array<out AudioDeviceInfo>) {
                    logger.info("Devices added: ${added.formatDevList()}")
                    refresh(this@audioManager)
                }

                override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>) {
                    logger.info("Devices removed: ${removed.formatDevList()}")
                    refresh(this@audioManager)
                }
            }, null)

            logger.info("AudioDeviceCallback registered (SDK ${Build.VERSION.SDK_INT}, usbTypes=${USB_TYPES.joinToString()}, modernBtTypes=${MODERN_BT_TYPES.joinToString()})")
        }

        patcher.after<DiscordAudioManager>("g") {
            val ble = modernBtDevice(e) ?: return@after

            synchronized(i) {
                if (r.first { it.a == DeviceTypes.BLUETOOTH_HEADSET }.b) {
                    logger.debug("g(): BT entry already available, skipping LE re-add")
                    return@after
                }

                logger.info("g(): stock rebuild dropped BT entry, re-adding LE device ${ble.formatDevName()} type=${ble.type}")
                forcedBtAvailable = true

                r = r.map {
                    if (it.a == DeviceTypes.BLUETOOTH_HEADSET)
                        AudioDevice(it.a, true, null, ble.formatDevName())
                    else it
                }

                s.k.onNext(r)
            }
        }

        // Stock b() routes Bluetooth through SCO only
        // Using setCommunicationDevice for LE audio
        patcher.before<DiscordAudioManager>("b", DeviceTypes::class.java) { (param, type: DeviceTypes) ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@before

            if (type == DeviceTypes.BLUETOOTH_HEADSET) {
                if (hasClassicSco(e)) {
                    logger.debug("b(BT): classic SCO device present, keeping stock SCO path")
                    return@before
                }
                val ble = modernBtDevice(e) ?: run {
                    logger.warn("b(BT): no classic SCO and no LE device found, letting stock path try anyway")
                    return@before
                }
                synchronized(i) {
                    if (!D) {
                        logger.debug("b(BT): not in communication mode (D=false), skipping LE routing")
                        return@before
                    }
                }
                logger.info("b(BT): routing via setCommunicationDevice to ${ble.formatDevName()} type=${ble.type}")
                k()

                val current = e.communicationDevice
                logger.info("b(BT): communicationDevice: $current")

                if (current?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    logger.debug("b(BT): clearing builtin speaker communication device first")
                    e.clearCommunicationDevice()
                }

                if (!e.setCommunicationDevice(ble)) {
                    logger.warn("setCommunicationDevice(${ble.productName}) failed, falling back to SCO")
                    return@before
                }

                logger.info("Routed voice to ${ble.productName} type=${ble.type} id=${ble.id} raw=$ble")
                synchronized(i) {
                    t = DeviceTypes.BLUETOOTH_HEADSET
                    u.k.onNext(t)
                }

                param.result = null
            } else {
                val current = e.communicationDevice
                if (current == null) {
                    logger.info("b($type): No LE communication device non-BT activation")
                } else if (current.type in MODERN_BT_TYPES) {
                    logger.info("b($type): clearing LE communication device ${current.formatDevName()} for non-BT activation")
                    e.clearCommunicationDevice()
                }
            }
        }
    }

    private fun refresh(audioManager: DiscordAudioManager) {
        logger.debug("ModernAudioDevices: refresh")

        val outputs = audioManager.e.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val usb = outputs.firstOrNull { it.isSink && it.type in USB_TYPES }
        val analog = outputs.any { it.isSink && it.type in ANALOG_TYPES }
        val ble = modernBtDevice(audioManager.e)

        logger.debug(
            "refresh: outputs=[${outputs.joinToString { "${it.type}:${it.formatDevName()}" }}] " +
                "usb=${usb?.formatDevName()} analog=$analog ble=${ble?.formatDevName()}"
        )

        val changed = synchronized(audioManager.i) {
            val updated = audioManager.r.map { d ->
                when (d.a) {
                    DeviceTypes.WIRED_HEADSET ->
                        AudioDevice(d.a, analog || usb != null, d.c, usb?.formatDevName() ?: d.d)
                    DeviceTypes.BLUETOOTH_HEADSET -> when {
                        !d.b && ble != null -> {
                            forcedBtAvailable = true
                            AudioDevice(d.a, true, null, ble.formatDevName())
                        }

                        d.b && ble == null && forcedBtAvailable && d.c == null -> {
                            forcedBtAvailable = false
                            AudioDevice(d.a, false, null, null)
                        }
                        else -> d
                    }
                    else -> d
                }
            }

            if (updated == audioManager.r) {
                logger.debug("refresh: device list unchanged, skipping propagation")
                return@synchronized false
            }

            logger.info("refresh: device list changed -> ${updated.joinToString { "${it.a}=${it.b}${it.d?.let { n -> " ($n)" } ?: ""}" }}")
            audioManager.r = updated
            audioManager.s.k.onNext(updated)
            true
        }

        if (changed) audioManager.l()
    }

    private fun modernBtDevice(am: AudioManager): AudioDeviceInfo? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        val outputs = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

        for (type in MODERN_BT_TYPES) {
            outputs.firstOrNull { it.isSink && it.type == type }?.let { return it }
        }

        return null
    }

    private fun hasClassicSco(am: AudioManager): Boolean =
        am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .any { it.isSink && it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }
}

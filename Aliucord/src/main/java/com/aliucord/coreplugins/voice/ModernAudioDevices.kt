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

    private fun AudioDeviceInfo.formatName(): String =
        "$productName${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) " ($address)" else ""}"

    private fun Array<out AudioDeviceInfo>.formatList(): String =
        joinToString { "${it.formatName()} type=${it.type} sink=${it.isSink}" }

    private fun AudioManager.outputs(): Array<AudioDeviceInfo> =
        getDevices(AudioManager.GET_DEVICES_OUTPUTS)

    // First present output whose type is in [list of types], sinks only.
    private fun AudioManager.firstOutputOfType(types: IntArray): AudioDeviceInfo? =
        outputs().firstOrNull { it.isSink && it.type in types }

    private fun AudioManager.modernBtDevice(): AudioDeviceInfo? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        for (type in MODERN_BT_TYPES) firstOutputOfType(intArrayOf(type))?.let { return it }
        return null
    }

    private fun AudioManager.hasClassicSco(): Boolean =
        outputs().any { it.isSink && it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }

    fun register(patcher: PatcherAPI) {
        logger.debug("ModernAudioDevices: register")

        patcher.after<DiscordAudioManager>(Context::class.java) audioManager@{
            e.registerAudioDeviceCallback(object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(added: Array<out AudioDeviceInfo>) {
                    logger.info("Devices added: ${added.formatList()}")
                    refresh(this@audioManager)
                }

                override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>) {
                    logger.info("Devices removed: ${removed.formatList()}")
                    refresh(this@audioManager)
                }
            }, null)

            logger.info("AudioDeviceCallback registered (SDK ${Build.VERSION.SDK_INT}, usbTypes=${USB_TYPES.joinToString()}, modernBtTypes=${MODERN_BT_TYPES.joinToString()})")
        }

        patcher.after<DiscordAudioManager>("g") {
            val ble = e.modernBtDevice() ?: return@after

            synchronized(i) {
                if (r.first { it.a == DeviceTypes.BLUETOOTH_HEADSET }.b) {
                    logger.debug("g(): BT entry already available, skipping LE re-add")
                    return@after
                }

                logger.info("g(): stock rebuild dropped BT entry, re-adding LE device ${ble.formatName()} type=${ble.type}")
                forcedBtAvailable = true

                r = r.map {
                    if (it.a == DeviceTypes.BLUETOOTH_HEADSET)
                        AudioDevice(it.a, true, null, ble.formatName())
                    else it
                }

                s.k.onNext(r)
            }
        }

        // Stock b() routes Bluetooth through SCO only
        // modern devices use setCommunicationDevice instead
        // classic devices (has SCO) keep the stock path
        patcher.before<DiscordAudioManager>("b", DeviceTypes::class.java) { (param, type: DeviceTypes) ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return@before

            if (type != DeviceTypes.BLUETOOTH_HEADSET) {
                // drop any modern device we set so the stock path can survive
                e.communicationDevice?.takeIf { it.type in MODERN_BT_TYPES }?.let {
                    logger.info("b($type): clearing modern comm device ${it.formatName()} for non-BT activation")
                    e.clearCommunicationDevice()
                }

                return@before
            }

            if (e.hasClassicSco()) {
                logger.debug("b(BT): classic SCO device present, keeping stock SCO path")
                return@before
            }

            val ble = e.modernBtDevice() ?: run {
                logger.warn("b(BT): no classic SCO and no modern BT device, letting stock path try")
                return@before
            }

            synchronized(i) {
                if (!D) {
                    logger.debug("b(BT): not in communication mode, skipping modern routing")
                    return@before
                }
            }

            logger.info("b(BT): routing via setCommunicationDevice to ${ble.formatName()} type=${ble.type}")
            k()

            if (e.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                logger.debug("b(BT): clearing builtin speaker communication device first")
                e.clearCommunicationDevice()
            }

            if (!e.setCommunicationDevice(ble)) {
                logger.warn("setCommunicationDevice(${ble.formatName()}) failed, falling back to SCO")
                return@before
            }

            logger.info("Routed voice to ${ble.formatName()} type=${ble.type}")

            synchronized(i) {
                t = DeviceTypes.BLUETOOTH_HEADSET
                u.k.onNext(t)
            }

            param.result = null
        }
    }

    private fun refresh(audioManager: DiscordAudioManager) {
        logger.debug("ModernAudioDevices: refresh")

        val outputs = audioManager.e.outputs()
        val usb = audioManager.e.firstOutputOfType(USB_TYPES)
        val analog = outputs.any { it.isSink && it.type in ANALOG_TYPES }
        val ble = audioManager.e.modernBtDevice()

        logger.debug("refresh: usb=${usb?.formatName()} analog=$analog ble=${ble?.formatName()} outputs=[${outputs.formatList()}]")

        val changed = synchronized(audioManager.i) {
            val updated = audioManager.r.map { d ->
                when (d.a) {
                    DeviceTypes.WIRED_HEADSET ->
                        AudioDevice(d.a, analog || usb != null, d.c, usb?.formatName() ?: d.d)
                    DeviceTypes.BLUETOOTH_HEADSET -> when {
                        // Modern BT device present but stock marked the entry as unavailable,
                        // this forces it to exist
                        !d.b && ble != null -> {
                            forcedBtAvailable = true
                            AudioDevice(d.a, true, null, ble.formatName())
                        }

                        // Clear the devices that are gone (id=null)
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
}

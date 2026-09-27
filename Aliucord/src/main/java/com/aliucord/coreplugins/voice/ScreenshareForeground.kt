package com.aliucord.coreplugins.voice

import android.app.NotificationManager
import com.aliucord.Logger
import com.aliucord.api.PatcherAPI
import com.aliucord.patcher.InsteadHook
import com.aliucord.patcher.PreHook
import com.aliucord.patcher.after
import com.discord.utilities.voice.ScreenShareManager
import com.discord.utilities.voice.VoiceEngineForegroundService
import com.discord.utilities.voice.VoiceEngineServiceController
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

internal object ScreenshareForeground {
    private val logger = Logger("ScreenshareForeground")

    // com.discord.utilities.voice.VoiceEngineForegroundService
    private const val NOTIFICATION_ID = 101

    private val companionField by lazy { VoiceEngineServiceController::class.java.getDeclaredField("Companion") }
    private val getController by lazy { companionField.type.getDeclaredMethod("getINSTANCE") }
    private val bindingField by lazy {
        VoiceEngineServiceController::class.java
            .getDeclaredField("serviceBinding")
            .apply { isAccessible = true }
    }
    private val managerField by lazy {
        VoiceEngineForegroundService::class.java
            .getDeclaredField("screenShareManager")
            .apply { isAccessible = true }
    }
    private val intentField by lazy {
        ScreenShareManager::class.java
            .getDeclaredField("screenshareIntent")
            .apply { isAccessible = true }
    }
    private val serviceCompanionField by lazy {
        VoiceEngineForegroundService::class.java
            .getDeclaredField("Companion")
            .apply { isAccessible = true }
    }
    private val stopForegroundAndUnbind by lazy {
        serviceCompanionField.type.getDeclaredMethod(
            "stopForegroundAndUnbind",
            VoiceEngineForegroundService.Connection::class.java,
        )
    }

    private var heldTeardown = false
    private var replaying = false

    fun register(patcher: PatcherAPI) = runCatching {
        val capturer = Class.forName("b.a.q.m0.b")
        val startCapture = capturer.getDeclaredMethod(
            "startCapture",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
        )

        // Sharing app audio retries forever if the screenshare dies
        patcher.patch(capturer.getDeclaredMethod("b"), InsteadHook.DO_NOTHING)

        patcher.patch(startCapture, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                promote()

                param.result = null

                runCatching {
                    XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args)
                }.onFailure {
                    logger.error("Screenshare capture could not start", it)
                }
            }
        })

        patcher.patch(stopForegroundAndUnbind, PreHook { param ->
            if (replaying || !isStreaming()) return@PreHook

            heldTeardown = true
            param.result = null

            logger.info("Held back the voice service teardown, a screenshare is still running")
        })

        // Remove the hold because the screenshare intent gets cleared here
        patcher.after<ScreenShareManager>("stopStream") { releaseTeardown() }

        logger.info("Registered")
    }.onFailure {
        logger.error("Failed to hook the voice service, screensharing may die early", it)
    }

    private fun releaseTeardown() {
        if (!heldTeardown) return
        heldTeardown = false

        val connection = boundConnection() ?: return
        logger.info("Screenshare ended, letting the voice service go")

        replaying = true

        runCatching {
            stopForegroundAndUnbind.invoke(serviceCompanionField[null], connection)
        }.onFailure {
            logger.error("Could not finish the held back teardown", it)
        }

        replaying = false
    }

    private fun isStreaming(): Boolean = runCatching {
        val service = boundConnection()?.service ?: return false
        val manager = managerField[service] ?: return false

        intentField[manager] != null
    }.getOrElse {
        logger.error("Could not get isStreaming status, is there any screenshare running?", it)
        false
    }

    private fun promote() {
        val service = boundConnection()?.service ?: return

        runCatching {
            val notification = service.getSystemService(NotificationManager::class.java)
                .activeNotifications
                .firstOrNull { it.id == NOTIFICATION_ID }
                ?.notification
                ?: return logger.debug("No voice notification to re-post, cannot re-promote")

            service.startForeground(NOTIFICATION_ID, notification)

            logger.debug("Voice service put back in the foreground")
        }.onFailure {
            logger.error("Could not restore the voice foreground service", it)
        }
    }

    // The controller already owns the binding, so it is read from there rather than tracked
    private fun boundConnection(): VoiceEngineForegroundService.Connection? = runCatching {
        val controller = getController.invoke(companionField[null])

        bindingField[controller] as? VoiceEngineForegroundService.Connection
    }.getOrElse {
        logger.error("Could not reach the voice service binding", it)
        null
    }
}

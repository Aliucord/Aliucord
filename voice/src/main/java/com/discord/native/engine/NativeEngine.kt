package com.discord.native.engine

import android.content.Context
import co.discord.media_engine.CameraEnumeratorProvider
import co.discord.media_engine.SharedEglBaseContext
import org.webrtc.EglBase
import org.webrtc.VideoFrame

@Suppress("unused")
class NativeEngine(
    context: Context,
    logLevel: Int,
) {
    private val nativeInstance: Long

    fun interface AudioInputInitializationCallback {
        fun onAudioInputInitialized(info: AudioInputInitializationInfo)
    }

    fun interface ConnectToServerCallback {
        fun onConnectToServer(info: ConnectionInfo, error: String)
    }

    fun interface DeviceChangeCallback {
        fun onChange(
            audioInputDevices: Array<AudioInputDeviceDescription?>,
            audioOutputDevices: Array<AudioOutputDeviceDescription?>,
            videoInputDevices: Array<VideoInputDeviceDescription?>
        )
    }

    fun interface GetAudioInputDevicesCallback {
        fun onDevices(devices: Array<AudioInputDeviceDescription?>)
    }

    fun interface GetAudioOutputDevicesCallback {
        fun onDevices(devices: Array<AudioOutputDeviceDescription?>)
    }

    fun interface GetAudioSubsystemCallback {
        fun onAudioSubsystem(subsystem: String, audioLayer: String)
    }

    fun interface GetCodecCapabilitiesCallback {
        fun onCodecCapabilities(codecs: String)
    }

    fun interface GetCodecSurveyCallback {
        fun onCodecSurvey(jsonStr: String)
    }

    fun interface GetRankedRtcRegionsCallback {
        fun onRankedRtcRegions(regions: Array<String>)
    }

    fun interface GetVideoInputDevicesCallback {
        fun onDevices(devices: Array<VideoInputDeviceDescription?>)
    }

    fun interface MLSSigningKeyCallback {
        fun onMLSSigningKey(key: String, signature: String)
    }

    fun interface OnNoInputCallback {
        fun onNoInput(input: Boolean)
    }

    fun interface OnVoiceCallback {
        fun onVoice(level: Float, speaking: Int)
    }

    fun interface StartLocalAudioRecordingCallback {
        fun onStartLocalAudioRecording(started: Boolean)
    }

    fun interface StopLocalAudioRecordingCallback {
        fun onStopLocalAudioRecording(fileName: String, durationMs: Int)
    }

    fun interface VideoFrameCallback {
        fun onFrame(frame: VideoFrame, mirror: Boolean): Boolean
    }

    init {
        val appCtx = context.applicationContext!!
        CameraEnumeratorProvider.maybeInit(appCtx)
        val eglCtx = SharedEglBaseContext.getEglContext()
        this.nativeInstance = nativeCreateInstance(
            appCtx,
            eglCtx,
            logLevel,
            context.getSharedPreferences("MediaEngine", 0).getBoolean("offloadAdmControls", false)
        )
    }

    private external fun nativeCreateInstance(context: Context, eglContext: EglBase.Context, logLevel: Int, offloadAdmControls: Boolean): Long

    private external fun nativeDestroyInstance()

    external fun createVoiceConnection(userId: String, connectionOptionsJSON: String, callback: ConnectToServerCallback): NativeConnection

    fun dispose() {
        nativeDestroyInstance()
    }

    external fun enableBuiltInAEC(enable: Boolean)

    external fun getAudioSubsystem(callback: GetAudioSubsystemCallback)

    external fun getCodecCapabilities(callback: GetCodecCapabilitiesCallback)

    external fun getCodecSurvey(callback: GetCodecSurveyCallback)

    external fun getInputDevices(callback: GetAudioInputDevicesCallback)

    external fun getMLSSigningKeyB64(sessionId: String, signatureVersion: Int, callback: MLSSigningKeyCallback)

    val maxSupportedProtocolVersion: Int
        external get

    external fun getOutputDevices(callback: GetAudioOutputDevicesCallback)

    external fun getVideoInputDevices(callback: GetVideoInputDevicesCallback)

    external fun rankRtcRegions(regionsWithIpsJSON: String, callback: GetRankedRtcRegionsCallback)

    external fun setAudioInputEnabled(enable: Boolean)

    external fun setAudioInputInitializationCallback(callback: AudioInputInitializationCallback)

    external fun setEmitVADLevel2(enable: Boolean)

    external fun setHasFullbandPerformance(hasFullbandPerformance: Boolean)

    external fun setInputDevice(deviceIndex: String)

    external fun setInputDeviceIndex(deviceIndex: Int)

    external fun setInputVolume(volume: Float)

    external fun setNoInputThreshold(threshold: Float)

    external fun setOnDeviceChangeCallback(callback: DeviceChangeCallback)

    external fun setOnNoInputCallback(callback: OnNoInputCallback)

    external fun setOnVoiceCallback(callback: OnVoiceCallback)

    external fun setOutputDevice(deviceIndex: String)

    external fun setOutputDeviceIndex(deviceIndex: Int)

    external fun setOutputVolume(volume: Float)

    external fun setSidechainCompression(enabled: Boolean)

    external fun setTransportOptions(optionsJSON: String)

    external fun setVideoInputDevice(deviceIndex: String)

    external fun setVideoInputDeviceIndex(deviceIndex: Int)

    external fun setVideoOutputSink(streamIdentifier: String, callback: VideoFrameCallback?)

    external fun startLocalAudioRecording(optionsJSON: String, callback: StartLocalAudioRecordingCallback)

    external fun stopLocalAudioRecording(callback: StopLocalAudioRecordingCallback)

    external fun updateFieldTrial(key: String, value: String)

    companion object {
        const val LOGLEVEL_DEBUG: Int = 1
        const val LOGLEVEL_DEFAULT: Int = 2

        init {
            System.loadLibrary("discord")
        }
    }
}

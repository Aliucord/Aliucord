package com.discord.native.engine

import android.content.Context
import co.discord.media_engine.CameraEnumeratorProvider
import co.discord.media_engine.SharedEglBaseContext
import com.google.gson.Gson
import org.webrtc.EglBase
import org.webrtc.VideoFrame

private val gson = Gson()

@Suppress("unused")
class NativeEngine(
    context: Context,
    private val logLevel: Int
) {
    private val nativeInstance: Long

    interface AudioInputInitializationCallback {
        fun onAudioInputInitialized(info: AudioInputInitializationInfo)
    }

    interface ConnectToServerCallback {
        fun onConnectToServer(info: ConnectionInfo, error: String)
    }

    interface DeviceChangeCallback {
        fun onChange(
            audioInputDevices: Array<AudioInputDeviceDescription?>,
            audioOutputDevices: Array<AudioOutputDeviceDescription?>,
            videoInputDevices: Array<VideoInputDeviceDescription?>
        )
    }

    interface GetAudioInputDevicesCallback {
        fun onDevices(devices: Array<AudioInputDeviceDescription?>)
    }

    interface GetAudioOutputDevicesCallback {
        fun onDevices(devices: Array<AudioOutputDeviceDescription?>)
    }

    interface GetAudioSubsystemCallback {
        fun onAudioSubsystem(subsystem: String, audioLayer: String)
    }

    interface GetCodecCapabilitiesCallback {
        fun onCodecCapabilities(codecs: String)
    }

    interface GetCodecSurveyCallback {
        fun onCodecSurvey(jsonStr: String)
    }

    interface GetRankedRtcRegionsCallback {
        fun onRankedRtcRegions(regions: Array<String>)
    }

    interface GetSupportedVideoCodecsCallback {
        fun onSupportedVideoCodecs(codecs: Array<String>)
    }

    interface GetVideoInputDevicesCallback {
        fun onDevices(devices: Array<VideoInputDeviceDescription?>)
    }

    interface MLSSigningKeyCallback {
        fun onMLSSigningKey(key: String, signature: String)
    }

    interface OnNoInputCallback {
        fun onNoInput(input: Boolean)
    }

    interface OnVoiceCallback {
        fun onVoice(level: Float, speaking: Int)
    }

    interface StartLocalAudioRecordingCallback {
        fun onStartLocalAudioRecording(started: Boolean)
    }

    interface StopLocalAudioRecordingCallback {
        fun onStopLocalAudioRecording(fileName: String, durationMs: Int)
    }

    interface VideoFrameCallback {
        fun onFrame(frame: VideoFrame, mirror: Boolean): Boolean
    }

    init {
        val appCtx = context.applicationContext!!
        CameraEnumeratorProvider.maybeInit(appCtx)
        val eglCtx = SharedEglBaseContext.getEglContext()
        this.nativeInstance = nativeCreateInstance(appCtx,
            eglCtx,
            logLevel,
            context.getSharedPreferences("MediaEngine", 0).getBoolean("offloadAdmControls", false))
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

    external fun getSupportedVideoCodecs(callback: GetSupportedVideoCodecsCallback)

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

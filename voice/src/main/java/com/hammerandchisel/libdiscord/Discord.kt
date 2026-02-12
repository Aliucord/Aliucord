package com.hammerandchisel.libdiscord

import android.content.Context
import android.util.Log
import co.discord.media_engine.*
import com.discord.native.engine.NativeEngine
import com.discord.native.engine.VideoInputDeviceFacing
import com.google.gson.Gson
import org.webrtc.VideoFrame

private val gson = Gson()

data class TransportOptions(
    val automaticGainControl: Boolean? = null,
    val automaticGainControlConfig: Any? = null,
    val av1Enabled: Boolean? = null,
    val builtInEchoCancellation: Boolean? = null,
    val bypassSystemProcessing: Boolean? = null,
    val ducking: Boolean? = null,
    val echoCancellation: Boolean? = null,
    val h264Enabled: Boolean? = null,
    val h265Enabled: Boolean? = null,
    val idleJitterBufferFlush: Boolean? = null,
    val noiseCancellation: Boolean? = null,
    val noiseCancellationAfterProcessing: Boolean? = null,
    val noiseCancellationDuringProcessing: Boolean? = null,
    val noiseSuppression: Boolean? = null,
    val vadAfterWebrtc: Boolean? = null,
    val voiceFilters: Boolean? = null,
)

@Suppress("ArrayInDataClass")
data class ConnectionOptions(
    val context: String,
    val address: String,
    val port: Int,
    val ssrc: Int,
    val experiments: Array<String>,
    val modes: Array<String>,
    val streamParameters: Array<Discord.NewStreamParameters>,
    val qosEnabled: Boolean,
)

@Suppress("unused")
class Discord @JvmOverloads constructor(private val context: Context, i: Int = -1) {
    private var localVoiceLevelChangedCallback: LocalVoiceLevelChangedCallback? = null
    private val nativeInstance: Long = 0
    private val nativeEngine: NativeEngine

    data class ConnectionInfo(
        @JvmField
        val isConnected: Boolean,
        @JvmField
        val protocol: String,
        @JvmField
        val localAddress: String,
        @JvmField
        val localPort: Int,
    )

    data class NewStreamParameters(
        val type: String, // "audio" | "video"
        val rid: String,
        val ssrc: Int,
        val maxBitrate: Int,

        val soundshare: Boolean? = null,

        val quality: Int? = null,
        val maxResolution: MaxResolution? = null,
        val rtxSsrc: Int? = null,
        val maxFrameRate: Int? = null,
        val active: Boolean? = null,
    ) {
        data class MaxResolution(
            val type: String, // "fixed"
            val width: Int,
            val height: Int,
        )

        companion object {
            fun from(old: StreamParameters): NewStreamParameters {
                return NewStreamParameters(
                    type = if (old.type == MediaType.Video) "video" else "audio",
                    rid = old.rid,
                    ssrc = old.ssrc,
                    rtxSsrc = old.rtxSsrc,
                    active = old.active,
                    maxBitrate = old.maxBitrate,
                    quality = old.quality,
                    // TODO
                    maxResolution = MaxResolution("fixed", 1280, 720),
                    maxFrameRate = 30,
                )
            }
        }
    }

    interface AecConfigCallback {
        fun onConfigureAEC(
            requestEnable: Boolean,
            enable: Boolean,
            requestMobileMode: Boolean,
            previouslyEnabled: Boolean,
            previouslyMobileMode: Boolean,
        )
    }

    interface BuiltinAECCallback {
        fun onConfigureBuiltinAEC(enabled: Boolean, requestEnabled: Boolean, available: Boolean)
    }

    interface ConnectToServerCallback {
        fun onConnectToServer(connectionInfo: ConnectionInfo, str: String)
    }

    interface GetAudioInputDevicesCallback {
        fun onDevices(audioInputDeviceDescriptionArr: Array<AudioInputDeviceDescription?>)
    }

    interface GetAudioOutputDevicesCallback {
        fun onDevices(audioOutputDeviceDescriptionArr: Array<AudioOutputDeviceDescription?>)
    }

    interface GetAudioSubsystemCallback {
        fun onAudioSubsystem(subsystem: String, audioLayer: String)
    }

    interface GetRankedRtcRegionsCallback {
        fun onRankedRtcRegions(regions: Array<String>)
    }

    interface GetSupportedVideoCodecsCallback {
        fun onSupportedVideoCodecs(codecs: Array<String>)
    }

    interface GetVideoInputDevicesCallback {
        fun onDevices(videoInputDeviceDescriptionArr: Array<VideoInputDeviceDescription?>)
    }

    interface LocalVoiceLevelChangedCallback {
        fun onLocalVoiceLevelChanged(f: Float, i: Int)
    }

    interface NoAudioInputCallback {
        fun onNoAudioInput(input: Boolean)
    }

    interface OnVideoCallback {
        fun onVideo(j: Long, i: Int, str: String, streamParametersArr: Array<StreamParameters?>)
    }

    interface VideoFrameCallback {
        fun onFrame(videoFrame: VideoFrame): Boolean
    }

    init {
        krispVersion = context.getString(R.string.krisp_model_version)
        CameraEnumeratorProvider.maybeInit(this.context)
        this.nativeEngine = NativeEngine(context, i)
        nativeEngine.setInputDevice("default")
        nativeEngine.setOutputDevice("default")
    }

    private fun setLocalVoiceLevelChangedCallbackNative(z2: Boolean) { }

    fun connectToServer(
        ssrc: Int,
        userId: Long,
        ip: String,
        port: Int,
        streamParametersArr: Array<StreamParameters>,
        connectToServerCallback: ConnectToServerCallback
    ): Connection {
        Log.i("Sunflower", "Hello!")

        val streamParams = arrayOf(
            NewStreamParameters(
                type = "audio",
                soundshare = false,
                maxBitrate = 64000,
                rid = "",
                ssrc = ssrc,
            ),
            NewStreamParameters.from(streamParametersArr[0])
        )
        val nativeConnection = nativeEngine.createVoiceConnection(
            userId.toString(),
            // TODO
            gson.m(ConnectionOptions(
                context = "default",
                address = ip,
                port = port,
                ssrc = ssrc,
                experiments = arrayOf(),
                modes = arrayOf("aead_aes256_gcm_rtpsize", "aead_xchacha20_poly1305_rtpsize"),
                streamParameters = streamParams,
                qosEnabled = false,
            ))
        ) { info, error ->
            connectToServerCallback.onConnectToServer(with(info) {
                ConnectionInfo(
                    isConnected,
                    protocol,
                    localAddress,
                    localPort,
                )
            }, error)
        }
        val conn = Connection(nativeConnection)
        return conn
    }

    fun crash() {} // only used in developer options
    fun dispose() = nativeEngine.dispose()

    fun enableBuiltInAEC(enabled: Boolean) = nativeEngine.enableBuiltInAEC(enabled)
    fun enableBuiltInAEC(enabled: Boolean, builtinAECCallback: BuiltinAECCallback?) {
        enableBuiltInAEC(enabled)
        // TODO
        builtinAECCallback?.onConfigureBuiltinAEC(enabled, requestEnabled = true, available = true)
    }

    fun getAudioInputDevices(getAudioInputDevicesCallback: GetAudioInputDevicesCallback) {
        nativeEngine.getInputDevices { devices ->
            getAudioInputDevicesCallback.onDevices(devices.map {
                it?.let {
                    AudioInputDeviceDescription(it.name, it.guid)
                }
            }.toTypedArray())
        }
    }

    fun getAudioOutputDevices(getAudioOutputDevicesCallback: GetAudioOutputDevicesCallback) {
        nativeEngine.getOutputDevices { devices ->
            getAudioOutputDevicesCallback.onDevices(devices.map {
                it?.let {
                    AudioOutputDeviceDescription(it.name, it.guid)
                }
            }.toTypedArray())
        }
    }

    fun getAudioSubsystem(getAudioSubsystemCallback: GetAudioSubsystemCallback) {
        nativeEngine.getAudioSubsystem { subsystem, audioLayer ->
            getAudioSubsystemCallback.onAudioSubsystem(subsystem, audioLayer)
        }
    }

    fun getRankedRtcRegions(rtcRegionArr: Array<RtcRegion>, getRankedRtcRegionsCallback: GetRankedRtcRegionsCallback) {
        nativeEngine.rankRtcRegions(gson.m(rtcRegionArr)) { regions ->
            getRankedRtcRegionsCallback.onRankedRtcRegions(regions)
        }
    }

    // TODO: last seen in 304.7/90.0.17-ptt-vad-arg-exposed
    // Likely reimpl in NativeEngine.getCodecCapabilities
    fun getSupportedVideoCodecs(getSupportedVideoCodecsCallback: GetSupportedVideoCodecsCallback) {
        getSupportedVideoCodecsCallback.onSupportedVideoCodecs(arrayOf("H264"))
        // nativeEngine.getSupportedVideoCodecs(object : NativeEngine.GetSupportedVideoCodecsCallback {
        //     override fun onSupportedVideoCodecs(codecs: Array<String>) {
        //         getSupportedVideoCodecsCallback.onSupportedVideoCodecs(codecs)
        //     }
        // })
    }

    fun getVideoInputDevices(getVideoInputDevicesCallback: GetVideoInputDevicesCallback) {
        nativeEngine.getVideoInputDevices { devices ->
            getVideoInputDevicesCallback.onDevices(devices.map {
                it?.let {
                    VideoInputDeviceDescription(it.name, it.guid, when (it.facing) {
                        VideoInputDeviceFacing.Back -> co.discord.media_engine.VideoInputDeviceFacing.Back
                        VideoInputDeviceFacing.Front -> co.discord.media_engine.VideoInputDeviceFacing.Front
                        VideoInputDeviceFacing.Unknown -> co.discord.media_engine.VideoInputDeviceFacing.Unknown
                    })
                }
            }.toTypedArray())
        }
    }

    fun setAudioInputEnabled(enabled: Boolean) = nativeEngine.setAudioInputEnabled(enabled)
    fun setAutomaticGainControl(enabled: Boolean) = TransportOptions(automaticGainControl = enabled).set()

    fun setEchoCancellation(enabled: Boolean) = TransportOptions(echoCancellation = enabled).set()
    fun setEchoCancellation(enabled: Boolean, z3: Boolean, aecConfigCallback: AecConfigCallback) {
        setEchoCancellation(enabled)
        aecConfigCallback.onConfigureAEC(true, enabled, true, !enabled, true)
    }

    // external fun setKeepAliveChannel(z2: Boolean)

    fun setLocalVoiceLevelChangedCallback(localVoiceLevelChangedCallback: LocalVoiceLevelChangedCallback?) {
        this.localVoiceLevelChangedCallback = localVoiceLevelChangedCallback
        setLocalVoiceLevelChangedCallbackNative(localVoiceLevelChangedCallback != null)
    }

    fun setMicVolume(volume: Float) = nativeEngine.setInputVolume(volume)

    fun setNoAudioInputCallback(noAudioInputCallback: NoAudioInputCallback) {
        nativeEngine.setOnNoInputCallback { input -> noAudioInputCallback.onNoAudioInput(input) }
    }

    fun setNoAudioInputThreshold(threshold: Float) = nativeEngine.setNoInputThreshold(threshold)

    fun setNoiseCancellation(enabled: Boolean) = TransportOptions(noiseCancellation = enabled).set()
    fun setNoiseSuppression(enabled: Boolean) = TransportOptions(noiseSuppression = enabled).set()
    fun setPlayoutDevice(deviceIndex: Int) = nativeEngine.setOutputDevice(if (deviceIndex == -1) "default" else deviceIndex.toString())
    fun setRecordingDevice(deviceIndex: Int) = nativeEngine.setInputDevice(if (deviceIndex == -1) "default" else deviceIndex.toString())
    fun setSpeakerVolume(volume: Float) = nativeEngine.setOutputVolume(volume)
    fun setVideoInputDevice(deviceIndex: Int) = nativeEngine.setVideoInputDevice(deviceIndex.toString())

    fun setVideoOutputSink(identifier: String, videoFrameCallback: VideoFrameCallback) {
        nativeEngine.setVideoOutputSink(identifier) { frame, mirror ->
            videoFrameCallback.onFrame(frame)
        }
    }

    private fun setTransportOptions(options: TransportOptions) {
        nativeEngine.setTransportOptions(gson.m(options))
    }
    private fun TransportOptions.set() = setTransportOptions(this)

    companion object {
        const val LOGLEVEL_DEBUG: Int = 2
        const val LOGLEVEL_DEFAULT: Int = -1

        @JvmStatic
        private var krispVersion: String = ""

        init {
            System.loadLibrary("discord")
        }
    }
}

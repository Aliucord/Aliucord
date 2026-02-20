package com.hammerandchisel.libdiscord

import android.content.Context
import android.util.Log
import co.discord.media_engine.*
import com.discord.native.engine.NativeEngine
import com.discord.native.engine.VideoInputDeviceFacing
import com.google.gson.Gson
import org.webrtc.VideoFrame

private val gson = Gson()

private data class TransportOptions(
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

@Suppress("unused")
class Discord @JvmOverloads constructor(private val context: Context, i: Int = -1) : IDiscord {
    private var localVoiceLevelChangedCallback: LocalVoiceLevelChangedCallback? = null
    private val nativeInstance: Long = 0
    private val nativeEngine: NativeEngine

    // START - Callback interfaces as defined in original class, do not edit!

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

    fun interface AecConfigCallback {
        fun onConfigureAEC(
            requestEnable: Boolean,
            enable: Boolean,
            requestMobileMode: Boolean,
            previouslyEnabled: Boolean,
            previouslyMobileMode: Boolean,
        )
    }

    fun interface BuiltinAECCallback {
        fun onConfigureBuiltinAEC(enabled: Boolean, requestEnabled: Boolean, available: Boolean)
    }

    fun interface ConnectToServerCallback {
        fun onConnectToServer(connectionInfo: ConnectionInfo, str: String)
    }

    fun interface GetAudioInputDevicesCallback {
        fun onDevices(audioInputDeviceDescriptionArr: Array<AudioInputDeviceDescription?>)
    }

    fun interface GetAudioOutputDevicesCallback {
        fun onDevices(audioOutputDeviceDescriptionArr: Array<AudioOutputDeviceDescription?>)
    }

    fun interface GetAudioSubsystemCallback {
        fun onAudioSubsystem(subsystem: String, audioLayer: String)
    }

    fun interface GetRankedRtcRegionsCallback {
        fun onRankedRtcRegions(regions: Array<String>)
    }

    fun interface GetSupportedVideoCodecsCallback {
        fun onSupportedVideoCodecs(codecs: Array<String>)
    }

    fun interface GetVideoInputDevicesCallback {
        fun onDevices(videoInputDeviceDescriptionArr: Array<VideoInputDeviceDescription?>)
    }

    fun interface LocalVoiceLevelChangedCallback {
        fun onLocalVoiceLevelChanged(f: Float, i: Int)
    }

    fun interface NoAudioInputCallback {
        fun onNoAudioInput(input: Boolean)
    }

    fun interface OnVideoCallback {
        fun onVideo(j: Long, i: Int, str: String, streamParametersArr: Array<StreamParameters?>)
    }

    fun interface VideoFrameCallback {
        fun onFrame(videoFrame: VideoFrame): Boolean
    }

    // END - Callback interfaces

    private data class ConnectionOptions(
        val context: String,
        val address: String,
        val port: Int,
        val ssrc: Int,
        val experiments: List<String>,
        val modes: List<String>,
        val streamParameters: List<NewStreamParameters>,
        val qosEnabled: Boolean,
    )

    private data class NewStreamParameters(
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

    init {
        krispVersion = context.getString(R.string.krisp_model_version)
        CameraEnumeratorProvider.maybeInit(this.context)
        this.nativeEngine = NativeEngine(context, i)
        nativeEngine.setInputDevice("default")
        nativeEngine.setOutputDevice("default")
    }

    private fun setLocalVoiceLevelChangedCallbackNative(z2: Boolean) { }

    override fun connectToServer(
        ssrc: Int,
        userId: Long,
        ip: String,
        port: Int,
        streamParametersArr: Array<StreamParameters>,
        connectToServerCallback: ConnectToServerCallback
    ): Connection {
        Log.i("Sunflower", "Hello!")

        val streamParams = listOf(
            NewStreamParameters(
                type = "audio",
                soundshare = false,
                maxBitrate = 64000, // TODO: 96/128 kbps?
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
                experiments = listOf(),
                modes = listOf("aead_aes256_gcm_rtpsize", "aead_xchacha20_poly1305_rtpsize"),
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

    override fun enableBuiltInAEC(enabled: Boolean, callback: BuiltinAECCallback?) {
        nativeEngine.enableBuiltInAEC(enabled)
        // TODO
        callback?.onConfigureBuiltinAEC(enabled, requestEnabled = true, available = true)
    }

    override fun getAudioSubsystem(callback: GetAudioSubsystemCallback) {
        nativeEngine.getAudioSubsystem { subsystem, audioLayer ->
            callback.onAudioSubsystem(subsystem, audioLayer)
        }
    }

    override fun getRankedRtcRegions(regions: Array<RtcRegion>, callback: GetRankedRtcRegionsCallback) {
        nativeEngine.rankRtcRegions(gson.m(regions)) { ranked ->
            callback.onRankedRtcRegions(ranked)
        }
    }

    // TODO: last seen in 304.7/90.0.17-ptt-vad-arg-exposed
    // Likely reimpl in NativeEngine.getCodecCapabilities
    override fun getSupportedVideoCodecs(callback: GetSupportedVideoCodecsCallback) {
        callback.onSupportedVideoCodecs(arrayOf("H264"))
        // nativeEngine.getSupportedVideoCodecs(object : NativeEngine.GetSupportedVideoCodecsCallback {
        //     override fun onSupportedVideoCodecs(codecs: Array<String>) {
        //         getSupportedVideoCodecsCallback.onSupportedVideoCodecs(codecs)
        //     }
        // })
    }

    override fun getVideoInputDevices(callback: GetVideoInputDevicesCallback) {
        nativeEngine.getVideoInputDevices { devices ->
            callback.onDevices(devices.map {
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

    override fun setAudioInputEnabled(enabled: Boolean) = nativeEngine.setAudioInputEnabled(enabled)
    override fun setAutomaticGainControl(enabled: Boolean) = TransportOptions(automaticGainControl = enabled).set()

    // No idea what z3 is, but it's always false
    override fun setEchoCancellation(enabled: Boolean, z3: Boolean, callback: AecConfigCallback?) {
        TransportOptions(echoCancellation = enabled).set()
        callback?.onConfigureAEC(true, enabled, true, !enabled, true)
    }

    override fun setLocalVoiceLevelChangedCallback(callback: LocalVoiceLevelChangedCallback?) {
        this.localVoiceLevelChangedCallback = callback
        setLocalVoiceLevelChangedCallbackNative(callback != null)
    }

    override fun setNoiseCancellation(enabled: Boolean) = TransportOptions(noiseCancellation = enabled).set()
    override fun setNoiseSuppression(enabled: Boolean) = TransportOptions(noiseSuppression = enabled).set()
    override fun setSpeakerVolume(volume: Float) = nativeEngine.setOutputVolume(volume)
    override fun setVideoInputDevice(deviceIndex: Int) = nativeEngine.setVideoInputDevice(deviceIndex.toString())

    override fun setVideoOutputSink(identifier: String, callback: VideoFrameCallback?) {
        nativeEngine.setVideoOutputSink(identifier) { frame, mirror ->
            callback?.onFrame(frame) == true
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

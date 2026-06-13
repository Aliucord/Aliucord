package com.hammerandchisel.libdiscord

import android.content.Context
import android.util.Log
import co.discord.media_engine.*
import com.discord.native.engine.NativeEngine
import com.google.gson.Gson
import org.webrtc.VideoFrame
import com.discord.native.engine.VideoInputDeviceFacing as NewVideoInputDeviceFacing

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

    data class CodecCapability(
        val codec: String,
        val decode: Boolean,
        val encode: Boolean,
    )

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

    init {
        krispVersion = context.getString(R.string.krisp_model_version)
        CameraEnumeratorProvider.maybeInit(this.context)
        this.nativeEngine = NativeEngine(context, i)
        TransportOptions(
            bypassSystemProcessing = true,
            ducking = false,
            idleJitterBufferFlush = true,
        ).set()
        nativeEngine.setSidechainCompression(true)
        nativeEngine.setInputDevice("default")
        nativeEngine.setOutputDevice("default")
        nativeEngine.setInputVolume(1f)
        nativeEngine.setOnVoiceCallback { level, speaking ->
            localVoiceLevelChangedCallback?.onLocalVoiceLevelChanged(level, speaking)
        }
        nativeEngine.setOnDeviceChangeCallback { audioInputDevices, audioOutputDevices, videoInputDevices ->
            val devices = audioInputDevices.toList() + audioOutputDevices + videoInputDevices
            Log.d("Sunflower", "Devices changed: ${devices.joinToString(", ")}")
        }
        nativeEngine.setAudioInputInitializationCallback {
            Log.d("Sunflower", "Audio input initialised in ${it.timeToInitializedNanos}ns: ${it.description}")
            nativeEngine.getAudioSubsystem { subsystem, audioLayer ->
                Log.d("Sunflower", "Subsystem $subsystem, audio layer $audioLayer")
            }
        }
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
        Log.i("Sunflower", "Connecting user $userId to $ip:$port (SSRC: $ssrc)")

        val nParams = NewStreamParameters.from(streamParametersArr[0])
        val streamParams = listOf(
            NewStreamParameters(
                type = "audio",
                soundshare = false,
                maxBitrate = 64000, // TODO: 96/128 kbps?
                rid = "",
                ssrc = ssrc,
            ),
            nParams
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
        return Connection(nativeConnection, nParams)
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

    // We *could* return the whole lot w/ decode/encode information,
    // replace b.a.q.m0.a (Codec) with new one with decode/encode fields,
    // then patch b.a.q.m0.c.p.onSupportedVideoCodecs to return full decode/encode info for
    // all codecs to send in select protocol; this potentially lets the server know we can
    // decode more things
    // However, RN just only uses codecs that are both decodable and encodable, so we will
    // copy that behaviour for now
    override fun getSupportedVideoCodecs(callback: GetSupportedVideoCodecsCallback) {
        nativeEngine.getCodecCapabilities { capabilitiesJson ->
            val capabilities = gson.g<Array<CodecCapability>>(capabilitiesJson, Array<CodecCapability>::class.java)
            Log.d("Sunflower", "Codec Capabilities: ${capabilities.contentToString()}")
            capabilities
                // TODO FIXME: changed to any decodable for video codec
                .filter { it.decode }
                // .filter { it.decode && it.encode }
                .map { it.codec }
                .let { callback.onSupportedVideoCodecs(it.toTypedArray()); Log.d("Sunflower", "Supported Codecs: $it") }
        }
    }

    override fun getVideoInputDevices(callback: GetVideoInputDevicesCallback) {
        nativeEngine.getVideoInputDevices { devices ->
            Log.i("Sunflower", "inputs: \n  - ${devices.joinToString("\n  - ")}")
            callback.onDevices(devices.map {
                it?.let {
                    VideoInputDeviceDescription(it.name, it.guid, when (it.facing) {
                        NewVideoInputDeviceFacing.Back -> VideoInputDeviceFacing.Back
                        NewVideoInputDeviceFacing.Front -> VideoInputDeviceFacing.Front
                        NewVideoInputDeviceFacing.Unknown -> VideoInputDeviceFacing.Unknown
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
        nativeEngine.setEmitVADLevel2(callback != null)
        this.localVoiceLevelChangedCallback = callback
    }

    override fun setNoiseCancellation(enabled: Boolean) = TransportOptions(noiseCancellation = enabled).set()
    override fun setNoiseSuppression(enabled: Boolean) = TransportOptions(noiseSuppression = enabled).set()
    override fun setSpeakerVolume(volume: Float) = nativeEngine.setOutputVolume(volume)

    // Noop, this is set in a .before patch in Sunflower core plugin, to use guids instead of indices
    override fun setVideoInputDevice(deviceIndex: Int) {}
    fun setVideoInputDevice(deviceGuid: String) = nativeEngine.setVideoInputDevice(deviceGuid)

    override fun setVideoOutputSink(identifier: String, callback: VideoFrameCallback?) {
        Log.i("Sunflower", "Outputsink set $identifier")
        nativeEngine.setVideoOutputSink(identifier) { frame, mirror ->
            // Log.i("Sunflower", "frame ${frame.timestampNs}")
            callback?.onFrame(frame) == true
        }
    }

    private fun setTransportOptions(options: TransportOptions) {
        Log.d("Sunflower", "engine/transportOptions: ${gson.m(options)}")
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

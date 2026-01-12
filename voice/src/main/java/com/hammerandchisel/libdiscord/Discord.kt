package com.hammerandchisel.libdiscord

import android.content.Context
import android.util.Log
import co.discord.media_engine.*
import com.discord.native.engine.*
import com.discord.native.engine.AudioInputDeviceDescription
import com.discord.native.engine.AudioOutputDeviceDescription
import com.discord.native.engine.VideoInputDeviceDescription
import com.discord.native.engine.VideoInputDeviceFacing
import com.google.gson.Gson
import org.webrtc.VideoFrame
import java.util.concurrent.Executors

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
    val streamParameters: Array<StreamParameters>,
    val qosEnabled: Boolean,
)

@Suppress("unused")
class Discord @JvmOverloads constructor(private val context: Context, i: Int = -1) {
    private var localVoiceLevelChangedCallback: LocalVoiceLevelChangedCallback? = null
    private val nativeInstance: Long = 0
    private val nativeEngine: NativeEngine

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

    interface GetAudioInputDevicesCallback {
        fun onDevices(audioInputDeviceDescriptionArr: Array<co.discord.media_engine.AudioInputDeviceDescription?>)
    }

    interface GetAudioOutputDevicesCallback {
        fun onDevices(audioOutputDeviceDescriptionArr: Array<co.discord.media_engine.AudioOutputDeviceDescription?>)
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
        fun onDevices(videoInputDeviceDescriptionArr: Array<co.discord.media_engine.VideoInputDeviceDescription?>)
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
        Log.i("Aliuvoice", "Hello from aliuvoice!")
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
                streamParameters = streamParametersArr,
                qosEnabled = false,
            )),
            object : NativeEngine.ConnectToServerCallback {
                override fun onConnectToServer(info: com.discord.native.engine.ConnectionInfo, error: String) {
                    connectToServerCallback.onConnectToServer(with (info) { ConnectionInfo(
                        isConnected,
                        protocol,
                        localAddress,
                        localPort,
                    ) }, error)
                }
            }
        )
        nativeConnection.prepareSecureFramesTransition(0, 0, object : NativeConnection.SecureFramesTransitionReadyCallback {
            override fun onTransitionReady() {
                nativeConnection.executeSecureFramesTransition(0)
            }
        })
        val conn = Connection(nativeConnection)
        Executors.newCachedThreadPool().execute {
            // conn.connectUser("{\\\"id\\\":\\\"184405311681986560\\\",\\\"videoSsrcs\\\":[],\\\"volume\\\":0.08483428955078125,\\\"ssrc\\\":709555,\\\"videoSsrc\\\":0,\\\"rtxSsrc\\\":0,\\\"mute\\\":false}")
            Thread.sleep(5000)
            conn.connectUser(184405311681986560L, 709555, 0, 0, false, 1f)
            Log.w("Aliuvoice", "User Connected")
        }
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
        nativeEngine.getInputDevices(object : NativeEngine.GetAudioInputDevicesCallback {
            override fun onDevices(devices: Array<AudioInputDeviceDescription?>) {
                getAudioInputDevicesCallback.onDevices(devices.map { it?.let {
                    co.discord.media_engine.AudioInputDeviceDescription(it.name, it.guid)
                } }.toTypedArray())
            }
        })
    }

    fun getAudioOutputDevices(getAudioOutputDevicesCallback: GetAudioOutputDevicesCallback) {
        nativeEngine.getOutputDevices(object : NativeEngine.GetAudioOutputDevicesCallback {
            override fun onDevices(devices: Array<AudioOutputDeviceDescription?>) {
                getAudioOutputDevicesCallback.onDevices(devices.map { it?.let {
                    co.discord.media_engine.AudioOutputDeviceDescription(it.name, it.guid)
                } }.toTypedArray())
            }
        })
    }

    fun getAudioSubsystem(getAudioSubsystemCallback: GetAudioSubsystemCallback) {
        nativeEngine.getAudioSubsystem(object : NativeEngine.GetAudioSubsystemCallback {
            override fun onAudioSubsystem(subsystem: String, audioLayer: String) {
                getAudioSubsystemCallback.onAudioSubsystem(subsystem, audioLayer)
            }
        })
    }

    fun getRankedRtcRegions(rtcRegionArr: Array<RtcRegion>, getRankedRtcRegionsCallback: GetRankedRtcRegionsCallback) {
        nativeEngine.rankRtcRegions(gson.m(rtcRegionArr), object : NativeEngine.GetRankedRtcRegionsCallback {
            override fun onRankedRtcRegions(regions: Array<String>) {
                getRankedRtcRegionsCallback.onRankedRtcRegions(regions)
            }
        })
    }

    fun getSupportedVideoCodecs(getSupportedVideoCodecsCallback: GetSupportedVideoCodecsCallback) {
        nativeEngine.getSupportedVideoCodecs(object : NativeEngine.GetSupportedVideoCodecsCallback {
            override fun onSupportedVideoCodecs(codecs: Array<String>) {
                getSupportedVideoCodecsCallback.onSupportedVideoCodecs(codecs)
            }
        })
    }

    fun getVideoInputDevices(getVideoInputDevicesCallback: GetVideoInputDevicesCallback) {
        nativeEngine.getVideoInputDevices(object : NativeEngine.GetVideoInputDevicesCallback {
            override fun onDevices(devices: Array<VideoInputDeviceDescription?>) {
                getVideoInputDevicesCallback.onDevices(devices.map { it?.let {
                    co.discord.media_engine.VideoInputDeviceDescription(it.name, it.guid, when (it.facing) {
                        VideoInputDeviceFacing.Back -> co.discord.media_engine.VideoInputDeviceFacing.Back
                        VideoInputDeviceFacing.Front -> co.discord.media_engine.VideoInputDeviceFacing.Front
                        VideoInputDeviceFacing.Unknown -> co.discord.media_engine.VideoInputDeviceFacing.Unknown
                    })
                } }.toTypedArray())
            }
        })
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
        nativeEngine.setOnNoInputCallback(object : NativeEngine.OnNoInputCallback {
            override fun onNoInput(input: Boolean) {
                noAudioInputCallback.onNoAudioInput(input)
            }
        })
    }

    fun setNoAudioInputThreshold(threshold: Float) = nativeEngine.setNoInputThreshold(threshold)

    fun setNoiseCancellation(enabled: Boolean) = TransportOptions(noiseCancellation = enabled).set()
    fun setNoiseSuppression(enabled: Boolean) = TransportOptions(noiseSuppression = enabled).set()
    fun setPlayoutDevice(deviceIndex: Int) = nativeEngine.setOutputDevice(if (deviceIndex == -1) "default" else deviceIndex.toString())
    fun setRecordingDevice(deviceIndex: Int) = nativeEngine.setInputDevice(if (deviceIndex == -1) "default" else deviceIndex.toString())
    fun setSpeakerVolume(volume: Float) = nativeEngine.setOutputVolume(volume)
    fun setVideoInputDevice(deviceIndex: Int) = nativeEngine.setVideoInputDevice(deviceIndex.toString())

    fun setVideoOutputSink(identifier: String, videoFrameCallback: VideoFrameCallback) {
        nativeEngine.setVideoOutputSink(identifier, object : NativeEngine.VideoFrameCallback {
            override fun onFrame(frame: VideoFrame, mirror: Boolean): Boolean {
                return videoFrameCallback.onFrame(frame)
            }
        })
    }

    private fun setTransportOptions(options: TransportOptions) {
        nativeEngine.setTransportOptions(gson.m(options))
    }
    private fun TransportOptions.set() = setTransportOptions(this)

    // external fun signalVideoOutputSinkReady(str: String)

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

package co.discord.media_engine

import co.discord.media_engine.internal.TransformStats
import com.discord.native.engine.NativeConnection
import com.google.gson.Gson
import org.webrtc.VideoCapturer

private val gson = Gson()

data class TransportAudioDecoder(
    val params: HashMap<String, String>,
    val channels: Int,
    val freq: Int,
    val name: String,
    val type: Int,
) {
    companion object {
        fun from(decoder: AudioDecoder): TransportAudioDecoder {
            with(decoder) {
                val params = HashMap<String, String>()
                paramsKeys.forEachIndexed { i, key -> params[key] = paramsValues[i] }
                return TransportAudioDecoder(params, channels, freq, name, type)
            }
        }
    }
}

data class TransportAudioEncoder(
    val type: Int,
    val rate: Int,
    val name: String,
    val freq: Int,
    val channels: Int,
    val pacsize: Int,
) {
    companion object {
        fun from(decoder: AudioEncoder): TransportAudioEncoder {
            with(decoder) {
                return TransportAudioEncoder(type, rate, name, freq, channels, pacsize)
            }
        }
    }
}

data class TransportVideoDecoder(
    val params: HashMap<String, String>,
    val channels: Int,
    val freq: Int,
    val name: String,
    val type: Int,
) {
    companion object {
        fun from(decoder: AudioDecoder) {
            with(decoder) {
                val params = HashMap<String, String>()
                paramsKeys.forEachIndexed { i, key -> params[key] = paramsValues[i] }
                TransportAudioDecoder(params, channels, freq, name, type)
            }
        }
    }
}

data class TransportOptions(
    val attenuateWhileSpeakingOthers: Boolean? = null,
    val attenuateWhileSpeakingSelf: Boolean? = null,
    val attenuation: Boolean? = null,
    val attenuationFactor: Double? = null,
    val audioDecoders: List<TransportAudioDecoder>? = null,
    val audioEncoder: TransportAudioEncoder? = null,
    val callBitRate: Int? = null,
    val callMaxBitRate: Int? = null,
    val callMinBitRate: Int? = null,
    val captureVideoFrameRate: Int? = null,
    val encodingVideoBitRate: Int? = null,
    val encodingVideoDegradationPreference: Int? = null,
    val encodingVideoFrameRate: Int? = null,
    val encodingVideoHeight: Int? = null,
    val encodingVideoMaxBitRate: Int? = null,
    val encodingVideoMinBitRate: Int? = null,
    val encodingVideoWidth: Int? = null,
    val encodingVoiceBitRate: Int? = null,
    val encryptionSettings: EncryptionSettings? = null,
    val experimentalEncoders: Boolean? = null,
    val fec: Boolean? = null,
    val hardwareH264: Boolean? = null,
    val inputMode: Int? = null,
    val inputModeOptions: Any? = null,
    val minimumJitterBufferLevel: Int? = null,
    val packetLossRate: Float? = null,
    val postponeDecodeLevel: Int? = null,
    val prioritySpeakerDucking: Double? = null,
    val qos: Boolean? = null,
    val reconnectInterval: Int? = null,
    val remoteAudioHistoryMs: Int? = null,
    val remoteSinkWantsMaxFramerate: Int? = null,
    val remoteSinkWantsPixelCount: Int? = null,
    val selfMute: Boolean? = null,
    val softwareH264: Boolean? = null,
    val streamParameters: Any? = null,
    val videoDecoders: Any? = null,
    val videoEncoder: Any? = null,
    val videoEncoderExperiments: String? = null,
) {
}

@Suppress("unused")
class Connection {
    private var disposed: Boolean = false
    private val nativeInstance: Long = 0
    private lateinit var native: NativeConnection

    private constructor(nativeInstance: Long) {
        throw IllegalStateException("ktConnection constructor called")
    }

    constructor(realInstance: NativeConnection) {
        this.native = realInstance
        this.native.setSecureFramesStateUpdateCallback(object : NativeConnection.SecureFramesStateUpdateCallback {
            override fun onSecureFramesStateUpdateCallback(stateUpdateJSON: String) {
            }
        })
    }

    interface EncryptionModesCallback {
        fun onEncryptionModes(strArr: Array<String?>?)
    }

    interface GetStatsCallback {
        fun onStats(stats: Stats?)

        fun onStatsError(th: Throwable?)
    }

    class GetStatsCallbackNative(private val callback: GetStatsCallback) {
        fun onStats(str: String) {
            try {
                this.callback.onStats(TransformStats.transform(str))
            } catch (e: Exception) {
                this.callback.onStatsError(e)
            }
        }
    }

    interface OnVideoCallback {
        fun onVideo(j: Long, i: Int, str: String?, streamParametersArr: Array<StreamParameters?>?)
    }

    object StatsFilter {
        const val ALL: Int = -1
        const val TRANSPORT: Int = 1
        const val OUTBOUND: Int = 2
        const val INBOUND: Int = 4
    }

    interface UserSpeakingStatusChangedCallback {
        fun onUserSpeakingStatusChanged(j: Long, z2: Boolean, z3: Boolean)
    }

    private fun getStatsNative(getStatsCallbackNative: GetStatsCallbackNative?, i: Int) {
        if (disposed) return
        native.getFilteredStats(i, object : NativeConnection.GetStatsCallback {
            override fun onStats(stats: String) {
                getStatsCallbackNative?.onStats(stats)
            }
        })
    }

    val userStreams = HashMap<Long, String>()

    data class UserConnectionInfo(
        val id: String,
        val videoSsrcs: List<Int>,
        val volume: Float,
        val ssrc: Int,
        val videoSsrc: Int,
        val rtxSsrc: Int,
        val mute: Boolean,
    )
    fun connectUser(userId: Long, audioSsrc: Int, txVideoSsrc: Int, rxVideoSsrc: Int, isMuted: Boolean, volume: Float) {
        native.mergeUsers(gson.m(listOf(UserConnectionInfo(
            id = userId.toString(),
            videoSsrcs = listOf(),
            volume = volume,
            ssrc = audioSsrc,
            videoSsrc = 0,
            rtxSsrc = 0,
            mute = isMuted,
        ))))
    }
    fun deafenLocalUser(deafened: Boolean) = native.setSelfDeafen(deafened)
    // TODO
    fun disableVideo(j: Long, z2: Boolean) {}
    // fun disableVideo(j: Long, z2: Boolean) = userStreams[j]?.let { engine.setVideoOutputSink(it, null) }
    fun disconnectUser(userId: Long) = native.destroyUser(userId.toString())
    fun dispose() {
        disposed = true
        native.dispose()
    }
    // TODO
    fun enableDiscontinuousTransmission(z2: Boolean) {}
    // TODO
    fun enableForwardErrorCorrection(z2: Boolean) {}
    fun getEncryptionModes(encryptionModesCallback: EncryptionModesCallback?) =
        native.getEncryptionModes(object : NativeConnection.GetEncryptionModesCallback {
            override fun onEncryptionModes(modes: Array<String?>) { encryptionModesCallback?.onEncryptionModes(modes) }
        })

    fun getStats(getStatsCallback: GetStatsCallback) {
        getStatsNative(GetStatsCallbackNative(getStatsCallback), -1)
    }

    fun getStats(getStatsCallback: GetStatsCallback, i: Int) {
        if (!disposed) getStatsNative(GetStatsCallbackNative(getStatsCallback), i)
    }

    fun muteLocalUser(z2: Boolean) {
        native.setSelfMute(z2)
        TransportOptions(selfMute = z2).set()
    }
    fun muteUser(j: Long, z2: Boolean) = native.setLocalMute(j.toString(), z2)

    // TODO
    fun setAudioInputMode(i: Int) {}

    // TODO
    fun setCodecs(
        audioEncoder: AudioEncoder,
        videoEncoder: VideoEncoder,
        audioDecoderArr: Array<AudioDecoder>,
        videoDecoderArr: Array<VideoDecoder>
    ) {
        TransportOptions(
            audioDecoders = audioDecoderArr.map { TransportAudioDecoder.from(it) },
            audioEncoder = TransportAudioEncoder.from(audioEncoder)
        ).set()
    }

    fun setEncodingQuality(minBitrate: Int, maxBitrate: Int, width: Int, height: Int, framerate: Int) {
        TransportOptions(
            encodingVideoDegradationPreference = 2, // TODO: ?
            encodingVideoBitRate = maxBitrate,
            encodingVideoMinBitRate = minBitrate,
            encodingVideoMaxBitRate = minBitrate,
            encodingVideoWidth = width,
            encodingVideoHeight = height,
            encodingVideoFrameRate = framerate,
        ).set()
    }

    fun setEncryptionSettings(settings: EncryptionSettings?) = TransportOptions(encryptionSettings = settings).set()
    fun setExpectedPacketLossRate(f: Float) = TransportOptions(packetLossRate = f).set()
    fun setMinimumPlayoutDelay(delay: Int) = native.setMinimumOutputDelay(delay)
    // TODO
    fun setOnVideoCallback(onVideoCallback: OnVideoCallback?) {}
    fun setPTTActive(active: Boolean) = native.setPTTActive(active, false) // TODO: priority?
    fun setQoS(enabled: Boolean) = TransportOptions(qos = enabled).set()
    fun setUserPlayoutVolume(userId: Long, volume: Float) = native.setLocalVolume(userId.toString(), volume)

    // TODO
    fun setVADAutoThreshold(i: Int) {}
    // TODO
    fun setVADLeadingFramesToBuffer(i: Int) {}
    // TODO
    fun setVADTrailingFramesToSend(i: Int) {}
    // TODO
    fun setVADTriggerThreshold(f: Float) {}
    // TODO
    fun setVADUseKrisp(z2: Boolean) {}

    fun setVideoBroadcast(broadcasting: Boolean) {
        if (disposed) return
        native.setVideoBroadcast(broadcasting)
    }

    // external fun simulatePacketLoss(f: Float)

    fun startScreenshareBroadcast(capturer: VideoCapturer, nativeInstance: Long) =
        native.startBroadcast(capturer, nativeInstance)
    fun stopScreenshareBroadcast() = native.stopBroadcast()

    fun setUserSpeakingStatusChangedCallback(userSpeakingStatusChangedCallback: UserSpeakingStatusChangedCallback) {
        native.setOnSpeakingCallback(object : NativeConnection.OnSpeakingCallback {
            override fun onSpeaking(userId: String, speakingFlags: Int, voiceDb: Float) {
                userSpeakingStatusChangedCallback.onUserSpeakingStatusChanged(userId.toLong(), speakingFlags > 0, false)
            }
        })
    }

    private fun setTransportOptions(options: TransportOptions) {
        native.setTransportOptions(gson.m(options))
    }
    private fun TransportOptions.set() = setTransportOptions(this)

    fun getMLSKeyPackageB64(callback: NativeConnection.MLSKeyPackageCallback) = native.getMLSKeyPackageB64(callback)

    fun prepareMLSCommitTransitionB64(transitionId: Int, commit: String, callback: NativeConnection.MLSCommitTransitionCallback) {
        native.prepareMLSCommitTransitionB64(transitionId, commit, callback)
    }

    fun prepareSecureFramesEpoch(epoch: String, transitionId: Int, groupId: String) {
        native.prepareSecureFramesEpoch(epoch, transitionId, groupId)
    }

    fun prepareSecureFramesTransition(transitionId: Int, protocolVersion: Int, callback: NativeConnection.SecureFramesTransitionReadyCallback) {
        native.prepareSecureFramesTransition(transitionId, protocolVersion, callback)
    }

    fun executeSecureFramesTransition(transitionId: Int) {
        native.executeSecureFramesTransition(transitionId)
    }

    fun processMLSProposalsB64(proposals: String, callback: NativeConnection.MLSProcessProposalsCallback) {
        native.processMLSProposalsB64(proposals, callback)
    }

    fun processMLSWelcomeB64(transitionId: Int, welcome: String, callback: NativeConnection.MLSWelcomeCallback) {
        native.processMLSWelcomeB64(transitionId, welcome, callback)
    }

    fun updateMLSExternalSenderB64(externalSenderB64: String) {
        native.updateMLSExternalSenderB64(externalSenderB64)
    }
}

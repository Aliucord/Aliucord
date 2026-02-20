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
)

@Suppress("unused")
class Connection : IConnection {
    fun interface EncryptionModesCallback {
        fun onEncryptionModes(strArr: Array<String?>?)
    }

    interface GetStatsCallback {
        fun onStats(stats: Stats?)
        fun onStatsError(th: Throwable?)
    }

    fun interface OnVideoCallback {
        fun onVideo(j: Long, i: Int, str: String?, streamParametersArr: Array<StreamParameters?>?)
    }

    fun interface UserSpeakingStatusChangedCallback {
        fun onUserSpeakingStatusChanged(j: Long, z2: Boolean, z3: Boolean)
    }

    object StatsFilter {
        const val ALL: Int = -1
        const val TRANSPORT: Int = 1
        const val OUTBOUND: Int = 2
        const val INBOUND: Int = 4
    }

    data class UserConnectionInfo(
        val id: String,
        val videoSsrcs: List<Int>,
        val volume: Float,
        val ssrc: Int,
        val videoSsrc: Int,
        val rtxSsrc: Int,
        val mute: Boolean,
    )

    private var disposed: Boolean = false
    private lateinit var native: NativeConnection

    private constructor(nativeInstance: Long) {
        throw IllegalStateException("ktConnection constructor called")
    }

    constructor(realInstance: NativeConnection) {
        this.native = realInstance
        // TODO
        // this.native.setSecureFramesStateUpdateCallback { }
    }

    override fun connectUser(userId: Long, audioSsrc: Int, txVideoSsrc: Int, rxVideoSsrc: Int, isMuted: Boolean, volume: Float) {
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
    override fun deafenLocalUser(isDeafened: Boolean) = native.setSelfDeafen(isDeafened)
    // TODO
    override fun disableVideo(userId: Long, isDisabled: Boolean) {}
    // fun disableVideo(j: Long, z2: Boolean) = userStreams[j]?.let { engine.setVideoOutputSink(it, null) }
    override fun dispose() {
        disposed = true
        native.dispose()
    }
    // TODO
    override fun enableForwardErrorCorrection(enabled: Boolean) {}
    fun getEncryptionModes(encryptionModesCallback: EncryptionModesCallback?) {
        native.getEncryptionModes { modes -> encryptionModesCallback?.onEncryptionModes(modes) }
    }

    override fun getStats(getStatsCallback: GetStatsCallback) = getStats(getStatsCallback, -1)

    override fun getStats(getStatsCallback: GetStatsCallback, filter: Int) {
        if (!disposed) {
            native.getFilteredStats(filter) { statsStr ->
                try {
                    getStatsCallback.onStats(TransformStats.transform(statsStr))
                } catch (e: Exception) {
                    getStatsCallback.onStatsError(e)
                }
            }
        }
    }

    override fun muteLocalUser(isMuted: Boolean) {
        native.setSelfMute(isMuted)
        TransportOptions(selfMute = isMuted).set()
    }
    override fun muteUser(userId: Long, isMuted: Boolean) = native.setLocalMute(userId.toString(), isMuted)

    // TODO
    override fun setAudioInputMode(mode: Int) {}

    // TODO
    override fun setCodecs(
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

    override fun setEncodingQuality(minBitrate: Int, maxBitrate: Int, width: Int, height: Int, framerate: Int) {
        TransportOptions(
            encodingVideoDegradationPreference = 2, // TODO: ?
            encodingVideoBitRate = maxBitrate,
            encodingVideoMinBitRate = minBitrate,
            encodingVideoMaxBitRate = maxBitrate,
            encodingVideoWidth = width,
            encodingVideoHeight = height,
            encodingVideoFrameRate = framerate,
        ).set()
    }

    override fun setEncryptionSettings(settings: EncryptionSettings) = TransportOptions(encryptionSettings = settings).set()
    override fun setExpectedPacketLossRate(lossRate: Float) = TransportOptions(packetLossRate = lossRate).set()
    // TODO
    override fun setOnVideoCallback(onVideoCallback: OnVideoCallback) {}
    override fun setPTTActive(isActive: Boolean) = native.setPTTActive(isActive, false) // TODO: priority?
    override fun setUserPlayoutVolume(userId: Long, volume: Float) = native.setLocalVolume(userId.toString(), volume)

    // TODO
    override fun setVADAutoThreshold(threshold: Int) {}
    // TODO
    override fun setVADLeadingFramesToBuffer(frameCount: Int) {}
    // TODO
    override fun setVADTrailingFramesToSend(frameCount: Int) {}
    // TODO
    override fun setVADTriggerThreshold(threshold: Float) {}
    // TODO
    override fun setVADUseKrisp(enabled: Boolean) {}

    override fun setVideoBroadcast(enabled: Boolean) {
        if (disposed) return
        native.setVideoBroadcast(enabled)
    }

    override fun startScreenshareBroadcast(videoCapturer: VideoCapturer, nativeInstance: Long) =
        native.startBroadcast(videoCapturer, nativeInstance)
    override fun stopScreenshareBroadcast() = native.stopBroadcast()

    override fun setUserSpeakingStatusChangedCallback(userSpeakingStatusChangedCallback: UserSpeakingStatusChangedCallback) {
        native.setOnSpeakingCallback { userId, speakingFlags, voiceDb ->
            userSpeakingStatusChangedCallback.onUserSpeakingStatusChanged(
                userId.toLong(),
                speakingFlags > 0,
                false
            )
        }
    }

    private fun setTransportOptions(options: TransportOptions) {
        native.setTransportOptions(gson.m(options))
    }
    private fun TransportOptions.set() = setTransportOptions(this)

    // New DAVE-related functions
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

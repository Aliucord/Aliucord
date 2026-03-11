package co.discord.media_engine

import android.util.Log
import com.discord.native.engine.NativeConnection
import com.google.gson.Gson
import com.hammerandchisel.libdiscord.Discord
import org.webrtc.VideoCapturer

private val gson = Gson()

private data class TransportOptions(
    val attenuateWhileSpeakingOthers: Boolean? = null,
    val attenuateWhileSpeakingSelf: Boolean? = null,
    val attenuation: Boolean? = null,
    val attenuationFactor: Double? = null,
    val audioDecoders: List<AudioDecoder>? = null,
    val audioEncoder: AudioEncoder? = null,
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
    val inputModeOptions: InputModeOptions? = null,
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
    val streamParameters: List<Discord.NewStreamParameters>? = null,
    val videoDecoders: List<VideoDecoder>? = null,
    val videoEncoder: VideoEncoder? = null,
    val videoEncoderExperiments: String? = null,
) {
    data class InputModeOptions(
        val vadAutoThreshold: Int? = null,
        val vadDuringPreProcess: Boolean? = null,
        val vadUseKrisp: Boolean? = null,
        val vadThreshold: Float? = null,
        val vadLeading: Int? = null,
        val vadTrailing: Int? = null,
        val vadKrispActivationThreshold: Float? = null,
    )
}

@Suppress("unused")
class Connection(private val native: NativeConnection, streamParameters: Discord.NewStreamParameters) : IConnection {
    fun interface EncryptionModesCallback {
        fun onEncryptionModes(strArr: Array<String?>?)
    }

    interface GetStatsCallback {
        fun onStats(stats: Stats?)
        fun onStatsError(th: Throwable?)
    }

    fun interface OnVideoCallback {
        fun onVideo(userId: Long, ssrc: Int, streamId: String, streamParametersArr: Array<StreamParameters>)
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

    init {
        // TODO
        this.native.setSecureFramesStateUpdateCallback {
            Log.d("Sunflower", "secureFramesUpdate: $it")
        }
        set(TransportOptions(
            encodingVideoDegradationPreference = 2,
            reconnectInterval = 60000,
            callMaxBitRate = 10000000,
            callBitRate = 600000,
            qos = false,
            attenuateWhileSpeakingSelf = false,
            callMinBitRate = 16000,
            attenuation = false,
            fec = true,
            inputModeOptions = TransportOptions.InputModeOptions(
                vadAutoThreshold = 3,
                vadDuringPreProcess = false,
                // TODO: Krisp is likely broken
                vadUseKrisp = false,
                vadThreshold = -60f,
                vadLeading = 5,
                vadTrailing = 25,
                vadKrispActivationThreshold = 0.5f,
            ),
            prioritySpeakerDucking = 0.1,
            packetLossRate = 0.3f,
            minimumJitterBufferLevel = 80,
            inputMode = 1,
            attenuationFactor = 1.0,
            encodingVoiceBitRate = 96000,
            postponeDecodeLevel = 100,
            attenuateWhileSpeakingOthers = true,
            selfMute = false,
            remoteAudioHistoryMs = 1000,
            streamParameters = listOf(streamParameters),
        ))
    }

    override fun connectUser(userId: Long, audioSsrc: Int, txVideoSsrc: Int, rxVideoSsrc: Int, isMuted: Boolean, volume: Float) {
        native.mergeUsers(gson.m(listOf(UserConnectionInfo(
            id = userId.toString(),
            videoSsrcs = listOf(txVideoSsrc),
            volume = volume,
            ssrc = audioSsrc,
            videoSsrc = txVideoSsrc,
            rtxSsrc = rxVideoSsrc,
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

    // TODO?
    override fun enableForwardErrorCorrection(enabled: Boolean) {}

    override fun getStats(getStatsCallback: GetStatsCallback) = getStats(getStatsCallback, -1)

    // TODO
    override fun getStats(getStatsCallback: GetStatsCallback, filter: Int) {
        // if (!disposed) {
        //     native.getFilteredStats(filter) { statsStr ->
        //         try {
        //             getStatsCallback.onStats(TransformStats.transform(statsStr))
        //         } catch (e: Exception) {
        //             getStatsCallback.onStatsError(e)
        //         }
        //     }
        // }
    }

    override fun muteLocalUser(isMuted: Boolean) {
        native.setSelfMute(isMuted)
        set(TransportOptions(selfMute = isMuted))
    }
    override fun muteUser(userId: Long, isMuted: Boolean) = native.setLocalMute(userId.toString(), isMuted)

    override fun setAudioInputMode(mode: Int) = set(TransportOptions(inputMode = mode))

    override fun setCodecs(
        audioEncoder: AudioEncoder,
        videoEncoder: VideoEncoder,
        audioDecoderArr: Array<AudioDecoder>,
        videoDecoderArr: Array<VideoDecoder>,
    ) {
        set(TransportOptions(
            audioEncoder = audioEncoder,
            videoEncoder = videoEncoder,
            audioDecoders = audioDecoderArr.toList(),
            videoDecoders = videoDecoderArr.apply { forEach { it.params.run {
                set("reset-on-errors", "1")
                set("fallback-on-consecutive-errors", "1")
            } } }.toList(),
        ))
    }

    override fun setEncodingQuality(minBitrate: Int, maxBitrate: Int, width: Int, height: Int, framerate: Int) {
        set(TransportOptions(
            encodingVideoDegradationPreference = 2, // TODO: ?
            encodingVideoBitRate = maxBitrate,
            encodingVideoMinBitRate = minBitrate,
            encodingVideoMaxBitRate = maxBitrate,
            encodingVideoWidth = width,
            encodingVideoHeight = height,
            encodingVideoFrameRate = framerate,
        ))
    }

    override fun setEncryptionSettings(settings: EncryptionSettings) = set(TransportOptions(encryptionSettings = settings))
    override fun setExpectedPacketLossRate(lossRate: Float) = set(TransportOptions(packetLossRate = lossRate))
    override fun setOnVideoCallback(onVideoCallback: OnVideoCallback) {
        native.setOnVideoCallback { userId, ssrc, streamId, videoStreamParametersJson ->
            onVideoCallback.onVideo(userId.toLong(), ssrc.toInt(), streamId, arrayOf())
        }
    }
    override fun setPTTActive(isActive: Boolean) = native.setPTTActive(isActive, priority = false, muteOverride = false) // TODO: priority? muteOverride?
    override fun setUserPlayoutVolume(userId: Long, volume: Float) = native.setLocalVolume(userId.toString(), volume)

    override fun setVADAutoThreshold(threshold: Int)
        = set(TransportOptions.InputModeOptions(vadAutoThreshold = threshold))
    override fun setVADLeadingFramesToBuffer(frameCount: Int)
        = set(TransportOptions.InputModeOptions(vadLeading = frameCount))
    override fun setVADTrailingFramesToSend(frameCount: Int)
        = set(TransportOptions.InputModeOptions(vadTrailing = frameCount))
    override fun setVADTriggerThreshold(threshold: Float)
        = set(TransportOptions.InputModeOptions(vadThreshold = threshold))
    override fun setVADUseKrisp(enabled: Boolean)
        = set(TransportOptions.InputModeOptions(vadUseKrisp = enabled))

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

    private fun set(options: TransportOptions) {
        Log.d("Sunflower", "connection/trwansportOptions: ${gson.m(options)}")
        native.setTransportOptions(gson.m(options))
    }

    private fun set(options: TransportOptions.InputModeOptions) =
        set(TransportOptions(inputModeOptions = options))

    // New DAVE-related functions
    fun connectUsers(userIds: List<String>) {
        val users = userIds.map { id ->
            UserConnectionInfo(
                id = id,
                videoSsrcs = listOf(),
                volume = 0f,
                ssrc = 0,
                videoSsrc = 0,
                rtxSsrc = 0,
                mute = false,
            )
        }
        native.mergeUsers(gson.m(users))
    }
    fun destroyUser(userId: String) = native.destroyUser(userId)

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

    fun setSecureFramesStateUpdateCallback(callback: NativeConnection.SecureFramesStateUpdateCallback) {
        native.setSecureFramesStateUpdateCallback(callback)
    }

    fun updateMLSExternalSenderB64(externalSenderB64: String) {
        native.updateMLSExternalSenderB64(externalSenderB64)
    }
}

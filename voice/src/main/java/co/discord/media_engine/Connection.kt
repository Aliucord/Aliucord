package co.discord.media_engine

import android.util.Log
import co.discord.media_engine.internal.TransformStats
import com.discord.native.engine.NativeConnection
import com.discord.native.engine.NativeEngine
import com.google.gson.Gson
import com.hammerandchisel.libdiscord.Discord
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.VideoCapturer
import kotlin.math.floor
import kotlin.math.roundToLong

private val gson = Gson()

private val floatStatsKeys = hashSetOf(
    "sumOfSquaredFramesDurations", "syncOffset", "targetDelay",
    "echoReturnLoss", "echoReturnLossEnchancement", "fractionLost",
    "residualEchoLikelihood", "residualEchoLikelihoodRecentMax"
)
private val nestedVideoKeys = arrayOf("rtpStats", "rtcpStats", "frameCounts")
private val nestedRtpKeys = arrayOf("transmitted", "retransmitted", "fec")

private data class NativeStreamParameters(
    val type: String? = null,
    val rid: String? = null,
    val ssrc: Int = 0,
    val rtxSsrc: Int = 0,
    val active: Boolean = false,
    val maxBitrate: Int = 0,
    val quality: Int = 0,
    val maxPixelCount: Int = 0,
)

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
class Connection(private val native: NativeConnection, streamParameters: List<Discord.NewStreamParameters>, private val engine: NativeEngine) : IConnection {
    companion object {
        @Volatile
        var priority: Boolean = false
    }

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

    object SpeakingFlags {
        const val MICROPHONE: Int = 1 shl 0
        const val SOUNDSHARE: Int = 1 shl 1
        const val PRIORITY: Int = 1 shl 2
    }

    data class UserConnectionInfo(
        val id: String,
        val audioSsrc: Int,
        val ssrc: Int = audioSsrc,
        val videoSsrcs: List<Int>,
        val rtxSsrcs: List<Int>,
        val volume: Float,
        val mute: Boolean,
    )

    @Suppress("PrivatePropertyName")
    private val TAG = "VoiceChatFix"
    private var disposed: Boolean = false
    private var loggedStatsFailure: Boolean = false
    @Volatile  // Last encoder cap from setEncodingQuality
    private var lastMaxBitrate: Int = Discord.DEFAULT_VIDEO_MAX_BITRATE

    init {
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
            streamParameters = streamParameters,
        ))
    }

    override fun connectUser(userId: Long, audioSsrc: Int, videoSsrc: Int, rtxSsrc: Int, isMuted: Boolean, volume: Float) {
        // volume here is RtcConnection.h(userId), stream-volume-boost amplitude NOT gain.
        // RtcConnection.v -> MediaEngineConnection.e)
        // mergeUsers raises exception:
        // Fatal signal 6 (SIGABRT), code -1 (SI_QUEUE) in tid ... (MediaEngineExec), pid ... (com.aliucord)
        // Only pass the video pair when both ssrcs are present.
        val createVolume = volume.coerceIn(0f, 1f)
        val hasVideo = videoSsrc != 0 && rtxSsrc != 0

        if (videoSsrc != 0 && rtxSsrc == 0) {
            Log.w(TAG, "connectUser userId=$userId videoSsrc=$videoSsrc without rtxSsrc, dropping video pair to avoid native assert")
        }

        val json = gson.m(listOf(UserConnectionInfo(
            id = userId.toString(),
            audioSsrc = audioSsrc,
            videoSsrcs = if (hasVideo) listOf(videoSsrc) else listOf(),
            rtxSsrcs = if (hasVideo) listOf(rtxSsrc) else listOf(),
            volume = createVolume,
            mute = isMuted,
        )))

        Log.d(TAG, "connectUser userId=$userId audioSsrc=$audioSsrc videoSsrc=$videoSsrc rtxSsrc=$rtxSsrc isMuted=$isMuted volume=$volume createVolume=$createVolume json=$json")

        native.mergeUsers(json)
    }
    override fun deafenLocalUser(isDeafened: Boolean) = native.setSelfDeafen(isDeafened)

    override fun disableVideo(userId: Long, isDisabled: Boolean) {
        Log.d(TAG, "disableVideo userId=$userId isDisabled=$isDisabled")
    }

    override fun dispose() {
        disposed = true
        native.dispose()
    }

    // TODO?
    override fun enableForwardErrorCorrection(enabled: Boolean) {}

    override fun getStats(getStatsCallback: GetStatsCallback) = getStats(getStatsCallback, StatsFilter.ALL)

    // TODO
    // Native reports stats as a JSON blob; stock's TransformStats (still in the base apk)
    // parses it into the Stats model. RtcStatsCollector polls this to feed VoiceQuality
    // (connection quality indicator + analytics) and KrispOveruseDetector - all starved
    // while this was stubbed. On shape drift we log once and hand back empty stats
    // instead of onStatsError: the stock error path logs the full stacktrace on EVERY
    // poll (~1/s) while empty stats degrade to "no data" quietly, same as the old stub.
    override fun getStats(getStatsCallback: GetStatsCallback, filter: Int) {
        if (disposed) return

        native.getFilteredStats(filter) { statsStr ->
            val sanitized = try {
                sanitizeStats(statsStr)
            } catch (e: Throwable) {
                if (!loggedStatsFailure) {
                    loggedStatsFailure = true
                    Log.w(TAG, "sanitizeStats failed, raw=${statsStr.take(512)}", e)
                }
                statsStr
            }

            try {
                getStatsCallback.onStats(TransformStats.transform(sanitized))
            } catch (e: Throwable) {
                if (!loggedStatsFailure) {
                    loggedStatsFailure = true
                    Log.w(TAG, "Failed to transform native stats inboundVideo=${firstInboundVideo(sanitized)} outboundSubstreams=${firstOutboundSubstreams(sanitized)} sanitized=${sanitized.take(1024)}", e)
                }
                getStatsCallback.onStats(
                    // empty stats
                    Stats(Transport(0, 0L, 0L, 0, 0, "", null), null, null, LinkedHashMap(), LinkedHashMap())
                )
            }
        }
    }

    // Between base and the latest, there has been some changes in the json data
    private fun sanitizeStats(statsStr: String): String {
        val root = JSONObject(statsStr)
        var changed = false
        root.optJSONArray("inbound")?.let { inbound ->
            var i = inbound.length() - 1
            while (i >= 0) {
                val entry = inbound.optJSONObject(i)
                if (entry != null) {
                    if (!entry.has("audio")) {
                        inbound.remove(i)
                        changed = true
                    } else {
                        if (entry.remove("playout") != null) changed = true
                        // the "videos" array is outbound-only and gson drops it here
                        if (normalizeInboundVideo(entry.optJSONObject("video"))) changed = true
                    }
                }
                i--
            }
        }
        root.optJSONObject("outbound")?.let { outbound ->
            if (normalizeOutboundVideos(outbound.optJSONArray("videos"))) changed = true
        }
        if (roundIntegralNumbers(root)) changed = true
        return if (changed) root.toString() else statsStr
    }

    private fun firstInboundVideo(statsStr: String): String = try {
        val inbound = JSONObject(statsStr).optJSONArray("inbound")
        var found: String? = null
        var i = 0
        while (found == null && inbound != null && i < inbound.length()) {
            found = inbound.optJSONObject(i)?.optJSONObject("video")?.toString()
            i++
        }
        found ?: "none"
    } catch (e: Throwable) {
        "unreadable"
    }

    private fun firstOutboundSubstreams(statsStr: String): String = try {
        JSONObject(statsStr).optJSONObject("outbound")
            ?.optJSONArray("videos")
            ?.optJSONObject(0)
            ?.optJSONArray("substreams")
            ?.toString()
            ?: "none"
    } catch (e: Throwable) {
        "unreadable"
    }

    private fun normalizeInboundVideo(video: JSONObject?): Boolean {
        video ?: return false
        var changed = video.backfillNames("decoderImplementationName")
        for (key in nestedVideoKeys) {
            if (video.ensureObject(key)) changed = true
        }
        return changed
    }

    private fun normalizeOutboundVideos(videos: JSONArray?): Boolean {
        videos ?: return false
        var changed = false
        for (i in 0 until videos.length()) {
            val video = videos.optJSONObject(i) ?: continue
            if (video.backfillNames("encoderImplementationName")) changed = true
            val substreams = video.optJSONArray("substreams") ?: continue
            for (j in 0 until substreams.length()) {
                val substream = substreams.optJSONObject(j) ?: continue
                for (key in nestedVideoKeys) {
                    if (substream.ensureObject(key)) changed = true
                }
                val rtpStats = substream.getJSONObject("rtpStats")
                for (key in nestedRtpKeys) {
                    if (rtpStats.ensureObject(key)) changed = true
                }
            }
        }
        return changed
    }

    private fun JSONObject.backfillNames(name: String): Boolean {
        var changed = false
        if (this.isNull(name)) {
            this.put(name, "")
            changed = true
        }
        if (this.isNull("codecName")) {
            this.put("codecName", "")
            changed = true
        }
        return changed
    }

    private fun JSONObject.ensureObject(key: String): Boolean {
        if (!this.isNull(key)) return false
        this.put(key, JSONObject())
        return true
    }

    private fun roundIntegralNumbers(value: Any?): Boolean {
        var changed = false
        when (value) {
            is JSONObject -> {
                val keys = value.keys()
                var fractional: ArrayList<String>? = null
                while (keys.hasNext()) {
                    val key = keys.next()
                    val child = value.opt(key)
                    if (child is JSONObject || child is JSONArray) {
                        if (roundIntegralNumbers(child)) changed = true
                    } else if (child is Number && key !in floatStatsKeys) {
                        val d = child.toDouble()
                        if (d != floor(d) || d.isInfinite()) {
                            (fractional ?: ArrayList<String>(4).also { fractional = it }).add(key)
                        }
                    }
                }
                val pending = fractional
                if (pending != null) {
                    for (key in pending) {
                        value.put(key, value.optDouble(key).roundToLong())
                    }
                    changed = true
                }
            }
            is JSONArray -> {
                for (i in 0 until value.length()) {
                    if (roundIntegralNumbers(value.opt(i))) changed = true
                }
            }
        }
        return changed
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
        if (maxBitrate > 0) lastMaxBitrate = maxBitrate
        set(TransportOptions(
            encodingVideoDegradationPreference = 2, // TODO: ?
            encodingVideoBitRate = maxBitrate,
            encodingVideoMinBitRate = minBitrate,
            encodingVideoMaxBitRate = maxBitrate,
            encodingVideoWidth = width,
            encodingVideoHeight = height,
            encodingVideoFrameRate = framerate,
            captureVideoFrameRate = framerate,
        ))
    }

    override fun setEncryptionSettings(settings: EncryptionSettings) = set(TransportOptions(encryptionSettings = settings))
    override fun setExpectedPacketLossRate(lossRate: Float) = set(TransportOptions(packetLossRate = lossRate.coerceIn(0f, 1f)))
    override fun setOnVideoCallback(onVideoCallback: OnVideoCallback) {
        native.setOnVideoCallback { userId, ssrc, streamId, videoStreamParametersJson ->
            Log.d(TAG, "onVideo userId=$userId ssrc=$ssrc streamId=$streamId params=$videoStreamParametersJson")

            val params = runCatching {
                // gson.f = fromJson(String, Class<T>)
                // `g` = (String, Type) overload
                // which erases <T> and breaks inference
                gson.f(videoStreamParametersJson, Array<NativeStreamParameters>::class.java)
                    .map { p ->
                        // Native leaves maxBitrate at 0, when calling Opcode 12,
                        // the stream never loads for the viewers
                        val maxBitrate = when {
                            p.maxBitrate > 0 -> p.maxBitrate
                            p.quality in 1..99 -> lastMaxBitrate / 3
                            else -> lastMaxBitrate
                        }

                        StreamParameters(
                            if (p.type == "audio") MediaType.Audio else MediaType.Video,
                            p.rid.orEmpty(),
                            p.ssrc,
                            p.rtxSsrc,
                            p.active,
                            maxBitrate,
                            p.quality,
                            p.maxPixelCount,
                        )
                    }.toTypedArray()
            }.getOrElse {
                Log.w(TAG, "Failed to parse native stream parameters, publishing empty streams", it)
                arrayOf()
            }

            onVideoCallback.onVideo(userId.toLong(), ssrc.toInt(), streamId, params)
        }
    }
    override fun setPTTActive(isActive: Boolean) {
        Log.d(TAG, "connection/setPTTActive isActive=$isActive priority=$priority")
        native.setPTTActive(isActive, priority, muteOverride = false)
    }
    fun setLocalPan(userId: Long, left: Float, right: Float) {
        Log.d(TAG, "connection/setLocalPan userId=$userId left=$left right=$right")
        native.setLocalPan(userId.toString(), left, right)
    }
    fun setPingInterval(intervalMs: Int) = native.setPingInterval(intervalMs)
    fun setMinimumOutputDelay(delayMs: Int) = native.setMinimumOutputDelay(delayMs)
    fun configureConnectionRetries(baseDelayMs: Int, maxDelayMs: Int, maxAttempts: Int) =
        native.configureConnectionRetries(baseDelayMs, maxDelayMs, maxAttempts)

    override fun setUserPlayoutVolume(userId: Long, volume: Float) {
        Log.d(TAG, "connection/setUserPlayoutVolume userId=$userId volume=$volume")
        native.setLocalVolume(userId.toString(), volume)
    }

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
        Log.d(TAG, "connection/setVideoBroadcast enabled=$enabled disposed=$disposed")
        if (disposed) return
        native.setVideoBroadcast(enabled)
    }

    override fun startScreenshareBroadcast(videoCapturer: VideoCapturer, nativeInstance: Long) {
        Log.d(TAG, "connection/startScreenshareBroadcast videoCapturer=$videoCapturer nativeInstance=$nativeInstance disposed=$disposed")
        if (disposed) return
        native.startBroadcast(videoCapturer, nativeInstance)
    }
    override fun stopScreenshareBroadcast() {
        Log.d(TAG, "connection/stopScreenshareBroadcast disposed=$disposed")
        if (disposed) return
        native.stopBroadcast()
    }

    override fun setUserSpeakingStatusChangedCallback(userSpeakingStatusChangedCallback: UserSpeakingStatusChangedCallback) {
        native.setOnSpeakingCallback { userId, speakingFlags, voiceDb ->
            Log.d(TAG, "connection/setUserSpeakingStatusChangedCallback: userId=${userId.toLong()} speakingFlags=$speakingFlags voiceDb=$voiceDb")
            userSpeakingStatusChangedCallback.onUserSpeakingStatusChanged(
                userId.toLong(),
                (speakingFlags and SpeakingFlags.MICROPHONE) != 0,
                (speakingFlags and SpeakingFlags.PRIORITY) != 0,
            )
        }
    }

    fun setRawTransportOptions(optionsJson: String) {
        Log.d(TAG, "connection/rawTransportOptions: $optionsJson")
        native.setTransportOptions(optionsJson)
    }

    private fun set(options: TransportOptions) {
        if (disposed) return
        val json = gson.m(options)
        Log.d(TAG, "connection/setTransportOptions: $json")
        native.setTransportOptions(json)
    }

    private fun set(options: TransportOptions.InputModeOptions) =
        set(TransportOptions(inputModeOptions = options))

    // New DAVE-related functions
    fun connectUsers(userIds: List<String>) {
        Log.d(TAG, "connection/connectUsers: $userIds")
        val users = userIds.map { id ->
            UserConnectionInfo(
                id = id,
                audioSsrc = 0,
                videoSsrcs = listOf(),
                rtxSsrcs = listOf(),
                volume = 0f,
                mute = false,
            )
        }
        native.mergeUsers(gson.m(users))
    }
    fun destroyUser(userId: String) {
        Log.d(TAG, "connection/destroyUser: $userId")
        native.destroyUser(userId)
    }

    fun getMLSKeyPackageB64(callback: NativeConnection.MLSKeyPackageCallback) = native.getMLSKeyPackageB64(callback)

    fun getMLSPairwiseFingerprintB64(version: Int, userId: String, callback: NativeConnection.MLSPairwiseFingerprintCallback) =
        native.getMLSPairwiseFingerprintB64(version, userId, callback)

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

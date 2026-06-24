package co.discord.media_engine

import co.discord.media_engine.Connection.*
import org.webrtc.VideoCapturer

/**
 * The interface for the original co.discord.media_engine.Connection class.
 * This should not be changed!!
 */

interface IConnection {
    fun connectUser(userId: Long, audioSsrc: Int, txVideoSsrc: Int, rxVideoSsrc: Int, isMuted: Boolean, volume: Float)
    fun deafenLocalUser(isDeafened: Boolean)
    fun disableVideo(userId: Long, isDisabled: Boolean)
    fun dispose()
    fun enableForwardErrorCorrection(enabled: Boolean)
    fun getStats(getStatsCallback: GetStatsCallback)
    fun getStats(getStatsCallback: GetStatsCallback, filter: Int)
    fun muteLocalUser(isMuted: Boolean)
    fun muteUser(userId: Long, isMuted: Boolean)
    fun setAudioInputMode(mode: Int)
    fun setCodecs(
        audioEncoder: AudioEncoder,
        videoEncoder: VideoEncoder,
        audioDecoderArr: Array<AudioDecoder>,
        videoDecoderArr: Array<VideoDecoder>,
    )
    fun setEncodingQuality(minBitrate: Int, maxBitrate: Int, width: Int, height: Int, framerate: Int)
    fun setEncryptionSettings(settings: EncryptionSettings)
    fun setExpectedPacketLossRate(lossRate: Float)
    fun setOnVideoCallback(onVideoCallback: OnVideoCallback)
    fun setPTTActive(isActive: Boolean)
    fun setUserPlayoutVolume(userId: Long, volume: Float)
    fun setUserSpeakingStatusChangedCallback(userSpeakingStatusChangedCallback: UserSpeakingStatusChangedCallback)
    fun setVADAutoThreshold(threshold: Int)
    fun setVADLeadingFramesToBuffer(frameCount: Int)
    fun setVADTrailingFramesToSend(frameCount: Int)
    fun setVADTriggerThreshold(threshold: Float)
    fun setVADUseKrisp(enabled: Boolean)
    fun setVideoBroadcast(enabled: Boolean)
    fun startScreenshareBroadcast(videoCapturer: VideoCapturer, nativeInstance: Long)
    fun stopScreenshareBroadcast()

    // These exist as part of the original class, but are otherwise unused
    // fun disconnectUser(j: Long)
    // fun enableDiscontinuousTransmission(z2: Boolean)
    // fun getEncryptionModes(encryptionModesCallback: EncryptionModesCallback?)
    // fun getNativeInstance(): Long
    // fun setMinimumPlayoutDelay(i: Int)
    // fun setQoS(z2: Boolean)
    // fun simulatePacketLoss(f: Float)
}

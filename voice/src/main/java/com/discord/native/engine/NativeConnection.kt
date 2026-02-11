package com.discord.native.engine

import org.webrtc.VideoCapturer

@Suppress("unused")
class NativeConnection {
    private val nativeInstance: Long = 0

    fun interface GetEncryptionModesCallback {
        fun onEncryptionModes(modes: Array<String?>)
    }

    fun interface GetStatsCallback {
        fun onStats(stats: String)
    }

    fun interface MLSCommitTransitionCallback {
        fun onMLSProcessedCommit(processedCommit: Boolean, protocolVersion: Int, rosterChange: String)
    }

    fun interface MLSFailureCallback {
        fun onMLSFailureCallback(source: String, reason: String)
    }

    fun interface MLSKeyPackageCallback {
        fun onMLSKeyPackage(keyPackageB64: String)
    }

    fun interface MLSPairwiseFingerprintCallback {
        fun onMLSPairwiseFingerprint(fingerprintB64: String)
    }

    fun interface MLSProcessProposalsCallback {
        fun onMLSCommitWelcome(commitWelcome: String)
    }

    fun interface MLSWelcomeCallback {
        fun onMLSProcessedWelcome(joinedGroup: Boolean, protocolVersion: Int, rosterChange: String)
    }

    fun interface OnFirstFrameCallback {
        fun onFirstFrame(userId: String, videoSsrc: Long, streamId: String)
    }

    fun interface OnPingCallback {
        fun onPing(ping: Int, server: String, port: Int, seq: Int)
    }

    fun interface OnPingTimeoutCallback {
        fun onPingTimeout(server: String, port: Int, seq: Int, timeout: Int)
    }

    fun interface OnSpeakingCallback {
        fun onSpeaking(userId: String, speakingFlags: Int, voiceDb: Float)
    }

    fun interface OnVideoCallback {
        fun onVideo(userId: String, ssrc: Long, streamId: String, videoStreamParametersJson: String)
    }

    fun interface SecureFramesStateUpdateCallback {
        fun onSecureFramesStateUpdateCallback(stateUpdateJSON: String)
    }

    fun interface SecureFramesTransitionReadyCallback {
        fun onTransitionReady()
    }

    private external fun nativeDestroyInstance()

    external fun configureConnectionRetries(baseDelayMs: Int, maxDelayMs: Int, maxAttempts: Int)

    external fun destroyUser(userId: String)

    fun dispose() {
        nativeDestroyInstance()
    }

    external fun executeSecureFramesTransition(transitionId: Int)

    external fun fastUdpReconnect()

    external fun getEncryptionModes(callback: GetEncryptionModesCallback)

    external fun getFilteredStats(filter: Int, callback: GetStatsCallback)

    external fun getMLSKeyPackageB64(callback: MLSKeyPackageCallback)

    external fun getMLSPairwiseFingerprintB64(version: Int, userId: String, callback: MLSPairwiseFingerprintCallback)

    external fun getStats(callback: GetStatsCallback)

    external fun mergeUsers(usersJSON: String)

    external fun prepareMLSCommitTransitionB64(transitionId: Int, commit: String, callback: MLSCommitTransitionCallback)

    external fun prepareSecureFramesEpoch(epoch: String, transitionId: Int, groupId: String)

    external fun prepareSecureFramesTransition(transitionId: Int, protocolVersion: Int, callback: SecureFramesTransitionReadyCallback)

    external fun processMLSProposalsB64(proposals: String, callback: MLSProcessProposalsCallback)

    external fun processMLSWelcomeB64(transitionId: Int, welcome: String, callback: MLSWelcomeCallback)

    external fun setDesktopSource(stringId: String, useVideoHook: Boolean, type: String)

    external fun setLocalMute(userId: String, mute: Boolean)

    external fun setLocalPan(userId: String, left: Float, right: Float)

    external fun setLocalVolume(userId: String, volume: Float)

    external fun setMinimumOutputDelay(delay: Int)

    external fun setNoInputThreshold(threshold: Float)

    external fun setOnFirstFrameCallback(callback: OnFirstFrameCallback)

    external fun setOnMLSFailureCallback(callback: MLSFailureCallback)

    external fun setOnPingCallback(callback: OnPingCallback)

    external fun setOnPingTimeoutCallback(callback: OnPingTimeoutCallback)

    external fun setOnSpeakingCallback(callback: OnSpeakingCallback)

    external fun setOnVideoCallback(callback: OnVideoCallback)

    external fun setPTTActive(active: Boolean, priority: Boolean)

    external fun setPingInterval(pingInterval: Int)

    external fun setSecureFramesStateUpdateCallback(callback: SecureFramesStateUpdateCallback)

    external fun setSelfDeafen(deafened: Boolean)

    external fun setSelfMute(muted: Boolean)

    external fun setTransportOptions(optionsJSON: String)

    external fun setVideoBroadcast(broadcasting: Boolean)

    external fun startBroadcast(capturer: VideoCapturer, soundshareNativeInstance: Long)

    external fun stopBroadcast()

    external fun updateMLSExternalSenderB64(externalSenderB64: String)
}

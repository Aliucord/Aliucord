package com.hammerandchisel.libdiscord

import co.discord.media_engine.*
import com.hammerandchisel.libdiscord.Discord.*

interface IDiscord {
    fun connectToServer(
        ssrc: Int,
        userId: Long,
        ip: String,
        port: Int,
        streamParametersArr: Array<StreamParameters>,
        connectToServerCallback: ConnectToServerCallback
    ): Connection

    fun enableBuiltInAEC(enabled: Boolean, callback: BuiltinAECCallback?)
    fun getAudioSubsystem(callback: GetAudioSubsystemCallback)
    fun getRankedRtcRegions(regions: Array<RtcRegion>, callback: GetRankedRtcRegionsCallback)
    fun getSupportedVideoCodecs(callback: GetSupportedVideoCodecsCallback)
    fun getVideoInputDevices(callback: GetVideoInputDevicesCallback)
    fun setAudioInputEnabled(enabled: Boolean)
    fun setAutomaticGainControl(enabled: Boolean)
    fun setEchoCancellation(enabled: Boolean, z3: Boolean, callback: AecConfigCallback?)
    fun setLocalVoiceLevelChangedCallback(callback: LocalVoiceLevelChangedCallback?)
    fun setNoiseCancellation(enabled: Boolean)
    fun setNoiseSuppression(enabled: Boolean)
    fun setSpeakerVolume(volume: Float)
    fun setVideoInputDevice(deviceIndex: Int)
    fun setVideoOutputSink(identifier: String, callback: VideoFrameCallback?)

    // These exist as part of the original class, but are otherwise unused
    // fun crash()
    // fun dispose()
    // @Deprecated("") fun enableBuiltInAEC(z2: Boolean)
    // fun getAudioInputDevices(getAudioInputDevicesCallback: GetAudioInputDevicesCallback)
    // fun getAudioOutputDevices(getAudioOutputDevicesCallback: GetAudioOutputDevicesCallback)
    // @Deprecated("") fun setEchoCancellation(z2: Boolean)
    // fun setKeepAliveChannel(z2: Boolean)
    // fun setMicVolume(f: Float)
    // fun setNoAudioInputCallback(noAudioInputCallback: NoAudioInputCallback)
    // fun setNoAudioInputThreshold(f: Float)
    // fun setPlayoutDevice(i: Int)
    // fun setRecordingDevice(i: Int)
    // fun signalVideoOutputSinkReady(str: String)
}

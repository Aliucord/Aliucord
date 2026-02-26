package co.discord.media_engine

import android.media.MediaCodecInfo
import org.webrtc.*
import org.webrtc.VideoEncoder

// Used by native code
@Suppress("unused")
class DiscordVideoEncoderFactory(context: EglBase.Context) : VideoEncoderFactory {
    private val fwd: HardwareVideoEncoderFactory =
        HardwareVideoEncoderFactory(context, false, true, PredicateSAM<MediaCodecInfo> { codecInfo ->
            !KNOWN_BAD_ENCODERS.contains(codecInfo.name.lowercase())
        })

    companion object {
        private val KNOWN_BAD_ENCODERS = HashSet<String?>(mutableListOf<String?>("c2.mtk.hevc.encoder", "omx.mtk.video.encoder.hevc"))
    }

    override fun createEncoder(videoCodecInfo: VideoCodecInfo): VideoEncoder? {
        return this.fwd.createEncoder(videoCodecInfo)
    }

    override fun getSupportedCodecs(): Array<VideoCodecInfo> {
        return this.fwd.getSupportedCodecs()
    }

    override fun getEncoderSelector(): VideoEncoderFactory.VideoEncoderSelector? = null
    override fun getImplementations(): Array<out VideoCodecInfo> = supportedCodecs
}

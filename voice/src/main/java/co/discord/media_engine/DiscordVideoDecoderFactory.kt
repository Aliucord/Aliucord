package co.discord.media_engine

import org.webrtc.*
import org.webrtc.VideoDecoder as WebrtcVideoDecoder

// Used by native code (Discord 333.5 media engine looks this class up via JNI FindClass)
@Suppress("unused")
class DiscordVideoDecoderFactory(context: EglBase.Context) : VideoDecoderFactory {
    private val hardwareVideoDecoderFactory: VideoDecoderFactory = HardwareVideoDecoderFactory(context)
    private val platformSoftwareVideoDecoderFactory: VideoDecoderFactory = PlatformSoftwareVideoDecoderFactory(context)
    private val softwareVideoDecoderFactory: VideoDecoderFactory = SoftwareVideoDecoderFactory()

    // 'createDecoder' overrides nothing.
    // @Deprecated("Legacy webrtc API", ReplaceWith("createDecoder(VideoCodecInfo)"))
    // override fun createDecoder(codecType: String): WebrtcVideoDecoder? = null

    override fun createDecoder(videoCodecInfo: VideoCodecInfo): WebrtcVideoDecoder? {
        var softwareDecoder = softwareVideoDecoderFactory.createDecoder(videoCodecInfo)
        val hardwareDecoder = hardwareVideoDecoderFactory.createDecoder(videoCodecInfo)
        if (softwareDecoder == null) {
            softwareDecoder = platformSoftwareVideoDecoderFactory.createDecoder(videoCodecInfo)
        }
        return when {
            hardwareDecoder != null && softwareDecoder != null ->
                VideoDecoderFallback(softwareDecoder, hardwareDecoder)

            hardwareDecoder != null -> hardwareDecoder
            else -> softwareDecoder
        }
    }

    override fun getSupportedCodecs(): Array<VideoCodecInfo> {
        val codecs = LinkedHashSet<VideoCodecInfo>()
        softwareVideoDecoderFactory.supportedCodecs
            .filter { it.name == "VP8" }
            .forEach { codecs.add(it) }
        codecs.addAll(hardwareVideoDecoderFactory.supportedCodecs)
        platformSoftwareVideoDecoderFactory.supportedCodecs
            .filter { it.name == "H264" }
            .forEach { codecs.add(it) }
        return codecs.toTypedArray()
    }
}

package com.discord;

import android.view.Surface;
import java.util.function.Consumer;
import org.webrtc.AndroidVideoDecoder;

/**
 * Stub Discord's surface-direct-decoder
 * StreamManager doesn't exist doesn't exist on the base version
 */
@SuppressWarnings("unused")
public class DiscordDecoderStreamManager {
    private static volatile DiscordDecoderStreamManagerDelegate delegate = null;
    private static volatile boolean surfaceDirectPathEnabled = false;

    public static void addOnConsumerReadyListener(String str, Consumer<Surface> consumer) {
        DiscordDecoderStreamManagerDelegate d = delegate;
        if (d != null) d.addOnConsumerReadyListener(str, consumer);
    }

    public static Surface getStreamConsumer(String str) {
        DiscordDecoderStreamManagerDelegate d = delegate;
        return d != null ? d.getStreamConsumer(str) : null;
    }

    public static boolean isSurfaceDirectPathEnabled() {
        return surfaceDirectPathEnabled;
    }

    public static void registerStreamProducer(String str, AndroidVideoDecoder androidVideoDecoder) {
        DiscordDecoderStreamManagerDelegate d = delegate;
        if (d != null) d.registerStreamProducer(str, androidVideoDecoder);
    }

    public static void removeOnConsumerReadyListener(String str, Consumer<Surface> consumer) {
        DiscordDecoderStreamManagerDelegate d = delegate;
        if (d != null) d.removeOnConsumerReadyListener(str, consumer);
    }

    public static DiscordDecoderStreamManagerDelegate setDelegate(DiscordDecoderStreamManagerDelegate discordDecoderStreamManagerDelegate) {
        delegate = discordDecoderStreamManagerDelegate;
        return discordDecoderStreamManagerDelegate;
    }

    public static void setSurfaceDirectPathEnabled(boolean z) {
        surfaceDirectPathEnabled = z;
    }
}

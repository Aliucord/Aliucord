package com.discord.play_delivery;

// Stub required by native code
/** @noinspection unused */
public class PlayAssetDeliveryNativeWrapper {
    private static volatile String krispAssetPackLocation = "";

    public static void setKrispAssetPackLocation(String location) {
        krispAssetPackLocation = location;
    }

    public static String getKrispAssetPackLocation() {
        return krispAssetPackLocation;
    }
}
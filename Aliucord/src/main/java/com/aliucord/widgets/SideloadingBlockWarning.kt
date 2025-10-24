package com.aliucord.widgets

import com.aliucord.Main
import com.aliucord.Utils
import com.aliucord.utils.ChangelogUtils

internal object SideloadingBlockWarning {
    fun openDialog() {
        val body = "# STOP, READ!" +
            "\n\u200b\n\n" +
            "## Google is planning to kill sideloading for ALL devices with Google Play Services installed!" +
            "\n\n" +

            "While the exact details of this change is not yet fully known, this could result in ALL sideloading " +
            "being blocked unless the app signer doxxes themselves to Google and/or publicly. Aliucord, Revanced, Bunny, " +
            "Revenge, and all other on-device signed mods would likely not be able to function under a system like this!" +
            "\n\n" +

            "If you are knowledgeable enough to install a custom ROM on your device, **NOW IS THE TIME TO DO IT**. " +
            "Do not install GAPPS (Google Play Services/GSF) as system/vendored apps (avoid system images with GAPPS included). " +
            "Always do your own research prior to installing custom ROMs! There may be one better suited for your " +
            "particular device." +
            "\n\n" +

            "- If you are located in the US, go to https://congress.gov/contact-us to contact your representatives & senators " +
            " and express your concern about Google's monopolistic and anti-consumer behavior!\n " +
            "- If you are located in the EU, contact your country's respective consumer protection agency here: " +
            "https://www.eccnet.eu/contact-your-local-ecc\n" +
            "- If you are located in another country, then contact your country's equivalent consumer protection agency!" +
            "\n\n" +

            "More info: https://arstechnica.com/gadgets/2025/08/google-will-block-sideloading-of-unverified-android-apps-starting-next-year/"

        try {
            ChangelogUtils.show(
                context = Utils.appActivity,
                version = "Important Notice",
                media = null,
                body = body,
            )
        } catch (t: Throwable) {
            Main.logger.error("Failed to show sideloading block warning dialog", t)
        }
    }

    fun maybeOpenDialog() {
        if (!Main.settings.getBool("sideloading_block_warning_seen", false)) {
            Main.settings.setBool("sideloading_block_warning_seen", true)
            openDialog()
        }
    }
}

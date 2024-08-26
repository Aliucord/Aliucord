package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.discord.stores.StoreExperiments
import com.discord.stores.StoreStream

/**
 * Discord wiped experiment defaults for Discord v126.21 so we have to set our own defaults now.
 */
internal class ExperimentDefaults : CorePlugin(Manifest("ExperimentDefaults")) {
    override val isHidden = true
    override val isRequired = true

    override fun start(context: Context) {
        // Credit to Dolfies for helping with these
        val newOverrides = arrayOf(
            "2020-09_threads" to 1,
            "2020-12_guild_delete_feedback" to 0, // analytics are not sent anyway, this is useless
            "2021-02_view_threads" to 1,
            "2021-03_android_extend_invite_expiration" to 1, // this matches with desktop
            "2021-03_android_media_sink_wants" to 1, // this might break video streaming if it doesn't work, but is useful if it does
            "2021-03_stop_offscreen_video_streams" to 1, // sounds pretty good if it works but idk
            "2021-04_contact_sync_android_main" to 1, // this system is i think deprecated but still functional?
            "2021-06_desktop_school_hubs" to 1,
            "2021-06_hub_email_connection" to 1,
            "2021-06_reg_bailout_to_email_android" to 1, // doesn't rly matter
            "2021-08_hub_multi_domain_mobile" to 1,
            "2021-08_threads_permissions" to 1,
            "2021-09_android_sms_autofill" to 1, // not sure if it works, doesn't rly matter
            "2021-10_android_attachment_bottom_sheet" to 1,
            "2021-10_study_group" to 1,
            "2021-11_guild_communication_disabled_guilds" to 1,
            "2021-11_guild_communication_disabled_users" to 1,
            "2021-11_hub_events" to 1,
            "2021-12_connected_accounts_playstation" to 2, // we don't want upsells
            "2022-01_email_change_confirmation" to 1,
            "2022-03_android_forum_channel_redesign" to 1,
            "2022-03_highlights_settings" to 1,
            "2022-03_text_in_voice" to 1,

            // 2021-10_premium_guild_member_profiles // you guys have special handling for this iirc
            // 2021-10_android_attachment_bottom_sheet // also already overridden iirc
            // 2021-08_android_speakerphone_default: // no clue
            // 2021-07_network_action_logging_android // no clue
            // 2021-05_opensl_default_enable_android // no clue, needs testing
            // 2020-12_invite_to_gdm // do whatever you like better xd
            // 2020-12_android_guild_channel_invite_sheet // same here
            // 2020-01_mobile_invite_suggestion_compact // and this
        )

        val experiments = StoreStream.getExperiments()
        val overrides = StoreExperiments.`access$getExperimentOverrides$p`(experiments)
        for ((key, value) in newOverrides) {
            if (overrides.containsKey(key)) continue
            else experiments.setOverride(key, value)
        }
    }

    override fun stop(context: Context) {}
}

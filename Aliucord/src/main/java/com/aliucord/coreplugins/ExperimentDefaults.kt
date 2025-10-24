package com.aliucord.coreplugins

import android.content.Context
import com.aliucord.entities.CorePlugin
import com.aliucord.patcher.*
import com.discord.stores.StoreExperiments
import com.discord.stores.StoreStream
import com.discord.utilities.features.GrowthTeamFeatures

/**
 * Discord wiped experiment defaults for Discord v126.21 so we have to set our own defaults now.
 */
internal class ExperimentDefaults : CorePlugin(Manifest("ExperimentDefaults")) {
    override val isHidden = true
    override val isRequired = true

    override fun start(context: Context) {
        // Thanks Dolfies for helping with this
        // Full list of v126.21 experiments: https://discord.com/channels/811255666990907402/811262084968742932/1276337938661118062
        val newOverrides = arrayOf(
            "2020-01_mobile_invite_suggestion_compact" to 0,
            "2020-09_threads" to 1,
            "2020-12_android_guild_channel_invite_sheet" to 0,
            "2020-12_guild_delete_feedback" to 0, // Analytics are not sent anyway, this is useless
            "2020-12_invite_to_gdm" to 0,
            "2021-02_view_threads" to 1,
            "2021-03_android_extend_invite_expiration" to 1, // This matches with desktop
            "2021-03_android_media_sink_wants" to 1,
            "2021-03_stop_offscreen_video_streams" to 1,
            "2021-04_contact_sync_android_main" to 1, // This system is deprecated I think but still functional?
            "2021-06_desktop_school_hubs" to 1,
            "2021-06_hub_email_connection" to 1,
            "2021-06_preview_promotions" to 1,
            "2021-06_reg_bailout_to_email_android" to 1, // Doesn't really matter
            "2021-07_network_action_logging_android" to 0,
            "2021-07_threads_only_channel" to 1,
            "2021-08_hub_multi_domain_mobile" to 1,
            "2021-08_threads_permissions" to 1,
            "2021-09_android_app_commands_frecency" to 1,
            "2021-09_android_sms_autofill" to 1,
            "2021-10_android_attachment_bottom_sheet" to 1,
            "2021-10_premium_guild_member_profiles" to 1,
            "2021-10_study_group" to 1,
            "2021-11_guild_communication_disabled_guilds" to 1,
            "2021-11_guild_communication_disabled_users" to 1,
            "2021-11_hub_events" to 1,
            "2021-12_connected_accounts_playstation" to 2, // We don't want upsells
            "2022-01_email_change_confirmation" to 1,
            "2022-03_android_forum_channel_redesign" to 1,
            "2022-03_highlights_settings" to 1,
            "2022-03_text_in_voice" to 1,
        )

        val experiments = StoreStream.getExperiments()
        val overrides = StoreExperiments.`access$getExperimentOverrides$p`(experiments)
        for ((key, value) in newOverrides) {
            if (!overrides.containsKey(key))
                experiments.setOverride(key, value)
        }

        // Silence the useless "Experiment Triggered: ..." message from logs
        patcher.instead<GrowthTeamFeatures>(
            "isBucketEnabled",
            Integer::class.java,
            String::class.java,
            Int::class.javaPrimitiveType!!,
            Boolean::class.javaPrimitiveType!!,
        ) { (_, assignedBucket: Int?, _: String, bucket: Int) ->
            return@instead assignedBucket == bucket
        }
    }

    override fun stop(context: Context) {}
}

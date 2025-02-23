/*
 * This file is part of Aliucord, an Android Discord client mod.
 * Copyright (c) 2023 Juby210 & Vendicated
 * Licensed under the Open Software License version 3.0
 */

package com.aliucord.coreplugins

import com.discord.restapi.RestAPIParams
import com.discord.utilities.rest.RestAPI
import com.discord.models.domain.ModelCustomStatusSetting
import com.aliucord.utils.RxUtils
import java.util.concurrent.TimeUnit
internal class AnimatedStatus : CorePlugin(Manifest("AnimatedStatus")) {
  
    @Suppress("UNCHECKED_CAST")
    override fun start(context: Context) {
      // Replace value as needed
      scheduleanimatedstatus()
    }

    fun scheduleanimatedstatus() {
      RxUtils.schedule(11, TimeUnit.SECONDS) {
        // Edit custom status
        RestAPI.getApi().updateUserSettingsCustomStatus(RestAPIParams.UserSettingsCustomStatus(ModelCustomStatusSetting("She", 0L, null, null))).subscribe {
          // Success change custom status
          RxUtils.schedule(11, TimeUnit.SECONDS) {
            RestAPI.getApi().updateUserSettingsCustomStatus(RestAPIParams.UserSettingsCustomStatus(ModelCustomStatusSetting("Very", 0L, null, null))).subscribe {
              RxUtils.schedule(11, TimeUnit.SECONDS) {
                RestAPI.getApi().updateUserSettingsCustomStatus(RestAPIParams.UserSettingsCustomStatus(ModelCustomStatusSetting("Much", 0L, null, null))).subscribe {
                  RxUtils.schedule(11, TimeUnit.SECONDS) {
                    RestAPI.getApi().updateUserSettingsCustomStatus(RestAPIParams.UserSettingsCustomStatus(ModelCustomStatusSetting("Loves", 0L, null, null))).subscribe {
                      RxUtils.schedule(11, TimeUnit.SECONDS) {
                        RestAPI.getApi().updateUserSettingsCustomStatus(RestAPIParams.UserSettingsCustomStatus(ModelCustomStatusSetting("Me!", 0L, null, null))).subscribe {
                        this.scheduleanimatedstatus()
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    }
}

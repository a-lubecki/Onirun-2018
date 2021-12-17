package com.onirun.model.bundle

import com.onirun.model.NotificationType

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
data class BundleNotificationSettingsList(val pushNotifs: BundleNotificationSettings = BundleNotificationSettings(false, false, false, false),
                                          val mailNotifs: BundleNotificationSettings = BundleNotificationSettings(false, false, false, false))
    : IParsableBundle {

    override fun isValid(): Boolean {
        return true
    }

    fun getPushEnable(type: NotificationType): Boolean? {
        return pushNotifs.getEnable(type)
    }

    fun getMailEnable(type: NotificationType): Boolean? {
        return mailNotifs.getEnable(type)
    }

}
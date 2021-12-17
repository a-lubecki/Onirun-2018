package com.onirun.model.bundle

import com.onirun.model.NotificationType

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
data class BundleNotificationSettings(val races: Boolean? = null, val friends: Boolean? = null,
                                      val suggest: Boolean? = null, val news: Boolean? = null)
    : IParsableBundle {

    override fun isValid(): Boolean {
        return true
    }

    fun getEnable(type: NotificationType): Boolean? {

        return when (type) {
            NotificationType.RACES -> races
            NotificationType.FRIENDS -> friends
            NotificationType.SUGGEST -> suggest
            NotificationType.NEWS -> news
        }
    }

}
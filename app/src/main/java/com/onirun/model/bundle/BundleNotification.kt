package com.onirun.model.bundle

import com.onirun.model.NotificationType

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 12/04/2018.
 */
class BundleNotification(val notifId: Int? = null, val seen: Boolean = false, val type: String = "",
                         val emoji: String = "", val message: String = "", val dateAdd: Long? = null,
                         val data: Map<String, Any?>? = null)
    : IParsableBundle {

    override fun isValid(): Boolean {
        return notifId != null &&
                notifId > 0 &&
                NotificationType.findNotificationType(type) != null &&
                message.isNotBlank() &&
                dateAdd != null
    }

}
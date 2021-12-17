package com.onirun.model.bundle

import com.onirun.model.Configuration

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 20/03/2018.
 */
data class BundleRunnerEvent(val eventId: Int? = null, val name: String = "",
                             val status: Int = 0, val department: String = "", val city: String = "",
                             val dateBegin: Long? = null, val dateEnd: Long? = null,
                             val raceTypes: List<String>? = null, val raceFormats: List<String>? = null,
                             val challenge: Boolean = false, val nbFriends: Int = 0, val engagement: Int = 0)
    : IParsableBundle {

    override fun isValid(): Boolean {
        return eventId != null &&
                eventId > 0 &&
                name.isNotBlank() &&
                Configuration.getInstance().hasDepartment(department) &&
                dateBegin != null &&
                dateEnd != null &&
                engagement >= 0
    }

}
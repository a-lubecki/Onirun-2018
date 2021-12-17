package com.onirun.model.bundle

import com.onirun.model.Configuration

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
data class BundleRace(val raceId: Int? = null, val eventId: Int? = null,
                      val name: String = "", val description: String = "",
                      val type: String = "", val formats: List<String>? = null, val price: String = "",
                      val address: String = "", val startTime: Long? = null, val length: Int? = null,
                      val event: BundleEvent? = null)
    : IParsableBundle {

    override fun isValid(): Boolean {
        return raceId != null &&
                raceId > 0 &&
                eventId != null &&
                eventId > 0 &&
                name.isNotBlank() &&
                Configuration.getInstance().hasRaceType(type)
    }

}
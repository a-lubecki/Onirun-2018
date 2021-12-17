package com.onirun.model.bundle

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 14/04/2018.
 */
data class BundleRaceRegistration(val raceId: Int? = null, val challenge: Boolean = false,
                                  val engagement: Int = 0, val nbFriends: Int = 0)
    : IParsableBundle {

    override fun isValid(): Boolean {
        return raceId != null &&
                raceId > 0 &&
                engagement >= 0 &&
                nbFriends >= 0
    }

}
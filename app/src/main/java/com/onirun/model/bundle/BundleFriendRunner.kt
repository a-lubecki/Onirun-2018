package com.onirun.model.bundle

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
data class BundleFriendRunner(val runnerId: Int? = null, val userName: String? = null,
                              val grade: String = "", val nbEvents: Int = 0)
    : IParsableBundle {

    override fun isValid(): Boolean {
        return runnerId != null &&
                runnerId > 0 &&
                nbEvents >= 0
    }

}
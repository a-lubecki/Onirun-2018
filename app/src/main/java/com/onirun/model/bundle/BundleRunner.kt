package com.onirun.model.bundle

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
data class BundleRunner(val runnerId: Int? = null, val userName: String = "",
                        val grade: String = "", val settings: BundleRunnerSettings? = null,
                        val nbFriends: Int = 0)
    : IParsableBundle {

    override fun isValid(): Boolean {
        return runnerId != null &&
                runnerId > 0 &&
                nbFriends >= 0
    }

}
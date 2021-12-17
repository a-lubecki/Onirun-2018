package com.onirun.model.bundle

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 24/03/2018.
 */
data class BundleRunnerGrade(val tag: String = "", val name: String = "")
    : IParsableBundle {

    override fun isValid(): Boolean {
        return tag.isNotEmpty() &&
                name.isNotBlank()
    }
}
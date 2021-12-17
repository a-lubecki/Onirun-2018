package com.onirun.model.bundle

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 12/04/2018.
 */
data class BundleArticleSummary(val slug: String = "", val title: String = "",
                                val subtitle: String = "", val illustration: String = "")
    : IParsableBundle {

    override fun isValid(): Boolean {
        return slug.isNotEmpty() &&
                title.isNotBlank()
    }
}
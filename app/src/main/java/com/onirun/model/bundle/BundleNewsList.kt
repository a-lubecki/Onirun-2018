package com.onirun.model.bundle

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 13/04/2018.
 */
data class BundleNewsList(private val content: List<BundleArticleSummary>? = null, val totalPages: Int = 0)
    : IParsableBundle {

    fun getNews(): List<BundleArticleSummary> {

        return content?.filter {
            it.isValid()
        } ?: listOf()
    }

    override fun isValid(): Boolean {
        return getNews().isNotEmpty() &&
                totalPages >= 0
    }

}
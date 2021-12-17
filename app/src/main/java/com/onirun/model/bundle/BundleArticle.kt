package com.onirun.model.bundle

import com.onirun.model.ArticleType

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 12/04/2018.
 */
data class BundleArticle(val slug: String = "", val title: String = "",
                         val illustration: String = "", val shareUrl: String = "",
                         val content: List<Map<String, Any?>>? = null, val type: String = "",
                         val event: Map<String, Any?>? = null, val vip: Map<String, Any?>? = null)
    : IParsableBundle {

    override fun isValid(): Boolean {
        return slug.isNotEmpty() &&
                title.isNotBlank() &&
                ArticleType.findArticleType(type) != null &&
                content != null
    }
}
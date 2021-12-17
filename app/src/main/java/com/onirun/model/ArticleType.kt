package com.onirun.model

/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
enum class ArticleType(val jsonName: String) {

    DEFAULT("default"),
    EVENT("event"),
    VIP("vip");

    companion object {

        fun findArticleType(jsonName: String?): ArticleType? {

            if (jsonName == null) {
                return null
            }

            return values().find { jsonName == it.jsonName }
        }
    }

}
package com.onirun.model

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.onirun.model.bundle.BundleRunnerEvent


enum class ArticleContentPartType(val jsonName: String) {

    PARAGRAPH("p") {

        override fun newArticleContentPart(fields: Map<String, Any?>): BaseArticleContentPart? {
            return ArticleContentParagraph(
                    this,
                    extractString(fields, "title"),
                    extractString(fields, "text")
            )
        }
    },
    QUOTE("quote") {

        override fun newArticleContentPart(fields: Map<String, Any?>): BaseArticleContentPart? {
            return ArticleContentQuote(
                    this,
                    extractString(fields, "text"),
                    extractString(fields, "author")
            )
        }
    },
    IMAGE("img") {

        override fun newArticleContentPart(fields: Map<String, Any?>): BaseArticleContentPart? {
            return ArticleContentImage(
                    this,
                    extractString(fields, "illustration")
            )
        }
    },
    VIDEO("video") {

        override fun newArticleContentPart(fields: Map<String, Any?>): BaseArticleContentPart? {
            return null //not supported yet
        }
    },
    EVENTS("events") {

        override fun newArticleContentPart(fields: Map<String, Any?>): BaseArticleContentPart? {

            val rawEvents = fields["events"] as? List<*> ?: return null

            val gson = Gson()

            val events = rawEvents.asSequence().mapNotNull {
                it as? LinkedTreeMap<*, *>
            }.mapNotNull {
                gson.toJsonTree(it).asJsonObject
            }.mapNotNull {
                gson.fromJson(it, BundleRunnerEvent::class.java)
            }.filter {
                it.isValid()
            }.map {
                RunnerEvent(it)
            }.toList()

            return ArticleContentEvents(
                    this,
                    events
            )
        }
    };

    abstract fun newArticleContentPart(fields: Map<String, Any?>): BaseArticleContentPart?

    companion object {

        fun findType(jsonName: String?): ArticleContentPartType? {

            if (jsonName == null) {
                return null
            }

            return values().find { jsonName == it.jsonName }
        }

        private fun extractString(fields: Map<String, Any?>, key: String): String {
            return fields[key] as? String ?: ""
        }
    }

}

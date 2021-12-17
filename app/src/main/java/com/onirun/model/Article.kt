package com.onirun.model

import com.onirun.model.bundle.BundleArticle

/**
 * Created by Aurelien Lubecki
 * on 20/03/2018.
 */
class Article(bundle: BundleArticle)
    : BaseArticle(bundle.slug, bundle.title, bundle.illustration) {

    val shareUrl = bundle.shareUrl
    val content by lazy {

        //convert every map values to objects
        bundle.content!!.mapNotNull {

            val type = ArticleContentPartType.findType(it["type"] as? String)

            type?.newArticleContentPart(it)
        }
    }

    val type = ArticleType.findArticleType(bundle.type)!!

    val specificEvent = bundle.event?.let {
        ArticleSpecificEvent(it)
    }
    val specificVIP = bundle.vip?.let {
        ArticleSpecificVIP(it)
    }

    class ArticleSpecificEvent(specific: Map<String, Any?>?) {

        val eventId = (specific?.get("eventId") as? Number)?.toInt()
        val department = (specific?.get("department") as? String)?.let {
            Configuration.getInstance().findDepartment(it)
        }
        val city = specific?.get("city") as? String ?: ""
    }

    class ArticleSpecificVIP(specific: Map<String, Any?>?) {

        val name = specific?.get("name") as? String ?: ""
        val facebook = specific?.get("facebook") as? String ?: ""
        val instagram = specific?.get("instagram") as? String ?: ""
        val twitter = specific?.get("twitter") as? String ?: ""
        val strava = specific?.get("strava") as? String ?: ""
    }
}



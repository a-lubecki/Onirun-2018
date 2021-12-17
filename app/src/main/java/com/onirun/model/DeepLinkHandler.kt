package com.onirun.model

import android.net.Uri
import com.onirun.BuildConfig

class DeepLinkHandler(uriDeepLink: Uri) {


    companion object {

        const val PATH_ACCEPT_RACE = "acceptRace"
        const val PATH_ACCEPT_FRIEND = "acceptFriend"
        const val PATH_EVENT = "event"
        const val PATH_ARTICLE = "article"

        const val PARAM_EVENT_ID = "eventId"
        const val PARAM_RACE_ID = "raceId"
        const val PARAM_RUNNER_ID = "runnerId"
        const val PARAM_ARTICLE_SLUG = "slug"

        fun newDeepLinkAcceptRace(runnerId: Int, eventId: Int, raceId: Int): Uri {

            return Uri.parse("https://${BuildConfig.ONIRUN_AUTHORITY_DYNAMIC_LINK}/${DeepLinkHandler.PATH_ACCEPT_RACE}" +
                    "?$PARAM_RUNNER_ID=$runnerId&$PARAM_EVENT_ID=$eventId&$PARAM_RACE_ID=$raceId"
            )
        }

        fun newDeepLinkAcceptFriend(runnerId: Int): Uri {

            return Uri.parse("https://${BuildConfig.ONIRUN_AUTHORITY_DYNAMIC_LINK}/${DeepLinkHandler.PATH_ACCEPT_FRIEND}" +
                    "?$PARAM_RUNNER_ID=$runnerId"
            )
        }
    }


    var type: DeepLinkType? = null

    var eventId: Int? = null
        private set
    var raceId: Int? = null
        private set
    var runnerId: Int? = null
        private set
    var articleSlug: String? = null
        private set

    var hasAcceptedRace = false
    var hasAcceptedFriend = false

    init {

        uriDeepLink.authority?.let { authority ->
            uriDeepLink.path?.let { path ->

                if (authority.contains("onirun")) {

                    when {

                        path.contains(PATH_ARTICLE) -> {

                            type = DeepLinkType.ARTICLE

                            //try to get the query param
                            articleSlug = uriDeepLink.getQueryParameter(PARAM_ARTICLE_SLUG)

                            if (articleSlug == null) {
                                //try to get the landing page slug
                                articleSlug = uriDeepLink.lastPathSegment
                            }
                        }

                        path.contains(PATH_ACCEPT_RACE) -> {

                            type = DeepLinkType.ACCEPT_RACE

                            eventId = uriDeepLink.getQueryParameter(PARAM_EVENT_ID)?.toIntOrNull()
                            raceId = uriDeepLink.getQueryParameter(PARAM_RACE_ID)?.toIntOrNull()
                            runnerId = uriDeepLink.getQueryParameter(PARAM_RUNNER_ID)?.toIntOrNull()
                        }

                        path.contains(PATH_ACCEPT_FRIEND) -> {

                            type = DeepLinkType.ACCEPT_FRIEND

                            runnerId = uriDeepLink.getQueryParameter(PARAM_RUNNER_ID)?.toIntOrNull()
                        }

                        path.contains(PATH_EVENT) -> {

                            type = DeepLinkType.EVENT

                            eventId = uriDeepLink.getQueryParameter(PARAM_EVENT_ID)?.toIntOrNull()
                        }
                    }

                }
            }
        }

    }


}
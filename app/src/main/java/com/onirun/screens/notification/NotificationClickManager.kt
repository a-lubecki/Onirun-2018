package com.onirun.screens.notification

import android.app.TaskStackBuilder
import android.content.Context
import com.mcxiaoke.koi.ext.Bundle
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.model.NotificationType
import com.onirun.screens.event.EventActivity
import com.onirun.screens.friends.FriendActivity
import com.onirun.screens.main.MainActivity
import com.onirun.screens.news.NewsArticleActivity
import com.onirun.utils.start
import com.onirun.utils.trackEvent


object NotificationClickManager {


    const val KEY_NOTIFICATION_ID = "notifId"
    const val KEY_NOTIFICATION_TYPE = "type"
    const val KEY_EVENT_ID = "eventId"
    const val KEY_FRIEND_ID = "friendId"
    const val KEY_SLUG = "slug"


    private fun extractEventId(data: Map<String, Any?>?): Int? {
        return extractId(data, KEY_EVENT_ID)
    }

    private fun extractFriendId(data: Map<String, Any?>?): Int? {
        return extractId(data, KEY_FRIEND_ID)
    }

    private fun extractSlug(data: Map<String, Any?>?): String? {
        return data?.get(KEY_SLUG) as? String
    }


    fun handleClick(context: Context, hasOpenedApp: Boolean, notifId: Int?, type: NotificationType?, data: Map<String, Any?>?): Boolean {

        //mark the notif as seen on the server
        if (notifId != null) {

            APIManager.call(
                    context,
                    {
                        it.putUserNotificationSeen(notifId)
                    },
                    true,
                    false,
                    null,
                    null
            )
        }

        if (type == null) {
            //can't process click
            return false
        }

        val nextIntent = when (type) {

            NotificationType.RACES,
            NotificationType.SUGGEST -> {

                extractEventId(data)?.let { eventId ->

                    EventActivity.newIntent(context, eventId)
                }
            }

            NotificationType.FRIENDS -> {

                extractFriendId(data)?.let { friendId ->

                    FriendActivity.newIntent(context, friendId)
                }
            }

            NotificationType.NEWS -> {

                extractSlug(data)?.let { slug ->

                    NewsArticleActivity.newIntent(context, slug)
                }
            }

        }

        if (nextIntent == null) {
            return false
        }

        if (hasOpenedApp) {

            TaskStackBuilder.create(context)
                    .addNextIntent(MainActivity.newIntentNewTask(context, R.id.notifications))
                    .addNextIntentWithParentStack(nextIntent)
                    .startActivities()

            //only track when the the app has been opened from a notif click
            when (type) {

                NotificationType.RACES -> {

                    context.applicationContext.trackEvent("race_notification", Bundle {

                        extractEventId(data)?.let { eventId ->
                            putInt("id_event", eventId)
                        }
                    })
                }

                NotificationType.SUGGEST -> context.applicationContext.trackEvent("suggest_notification")

                NotificationType.FRIENDS -> context.applicationContext.trackEvent("friend_notification")

                NotificationType.NEWS -> context.applicationContext.trackEvent("news_notification", Bundle {

                    extractSlug(data)?.let { slug ->
                        putString("slug", slug)
                    }
                })
            }

        } else {

            nextIntent.start(context)
        }

        return true
    }

    private fun extractId(data: Map<String, Any?>?, key: String): Int? {
        return (data?.get(key) as? Number)?.toInt()
    }

}
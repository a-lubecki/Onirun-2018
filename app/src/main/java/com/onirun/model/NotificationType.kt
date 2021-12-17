package com.onirun.model

/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
enum class NotificationType(val jsonName: String) {

    RACES("races"),
    FRIENDS("friends"),
    SUGGEST("suggest"),
    NEWS("news");

    companion object {

        fun findNotificationType(jsonName: String?): NotificationType? {

            if (jsonName == null) {
                return null
            }

            return values().find { jsonName == it.jsonName }
        }
    }

}
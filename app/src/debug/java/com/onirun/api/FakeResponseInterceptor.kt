package com.onirun.api

import android.content.Context
import com.onirun.R
import okhttp3.*


/**
 * Created by Aurelien Lubecki
 * on 15/04/2018.
 */
class FakeResponseInterceptor(val context: Context) : Interceptor {


    override fun intercept(chain: Interceptor.Chain?): Response {

        val url = chain!!.request().url().encodedPath().toString()
        val params = chain.request().url().query().toString()

        val code = 200

        val responseString = createJSONFromFile(when {

            url.endsWith("/configuration") -> R.raw.json_configuration

            url.endsWith("/home") || url.endsWith("/calendar") -> {

                if (url.contains("page=0")) {
                    R.raw.json_home_0
                } else {
                    R.raw.json_home_1
                }
            }

            url.contains("/events/") -> R.raw.json_event

            url.endsWith("/friends") -> {

                if (chain.request().method() == "GET") {
                    R.raw.json_friends
                } else {
                    0
                }
            }

            url.endsWith("/users/termsConsent") -> {

                if (chain.request().method() == "GET") {
                    R.raw.json_false
                } else {
                    0
                }
            }

            url.endsWith("/runners") || Regex(".+/runners/[0-9]+").matches(url) -> R.raw.json_runner

            url.endsWith("/runners/settings") -> R.raw.json_settings

            url.endsWith("/users/notifications") -> R.raw.json_notifications

            url.endsWith("/settings/departments") -> R.raw.json_settings_departments

            url.endsWith("/settings/raceTypes") -> R.raw.json_settings_race_types

            url.endsWith("/settings/raceFormats") -> R.raw.json_settings_race_formats

            url.endsWith("/settings/notifications") -> R.raw.json_settings_notifications

            url.endsWith("/runners/invite") -> {

                if (params.contains("raceId")) {
                    R.raw.json_invite_race
                } else {
                    R.raw.json_invite_friend
                }
            }

            url.endsWith("/registrations") -> null

            url.endsWith("/profile") -> {

                if (chain.request().method() == "GET") {
                    R.raw.json_profile
                } else {
                    0
                }
            }

            url.endsWith("/news") -> R.raw.json_news

            url.contains("/news/1") -> R.raw.json_news_article_1
            url.contains("/news/2") -> R.raw.json_news_article_2
            url.contains("/news/3") -> R.raw.json_news_article_3

            else -> 0
        })

        if (responseString == null) {
            //send request normally
            return chain.proceed(chain.request())
        }

        return Response.Builder()
                .code(code)
                .message(responseString)
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .body(ResponseBody.create(MediaType.parse("application/json"), responseString))
                .addHeader("content-type", "application/json")
                .build()
    }

    private fun createJSONFromFile(fileID: Int?): String? {

        if (fileID == null) {
            return null
        }

        // Read file into string builder
        try {

            context.resources.openRawResource(fileID).use {
                return it.reader().readText()
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return ""
    }

}
package com.onirun.api

import com.onirun.model.bundle.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Aurelien Lubecki
 * on 10/04/2018.
 */
interface APIService {


    @GET("configuration")
    fun getConfiguration(): Call<BundleConfiguration>

    @GET("runners")
    fun getRunner(): Call<BundleRunner>

    @GET("runners/{runnerId}")
    fun getRunner(
            @Path("runnerId") runnerId: Int
    ): Call<BundleRunner>

    @PUT("runners/userName")
    fun putRunnerUserName(
            @Body userName: String
    ): Call<ResponseBody>

    @GET("runners/profile")
    fun getRunnerProfile(): Call<BundleRunnerProfile>

    @PUT("runners/profile")
    fun putRunnerProfile(
            @Body profile: BundleRunnerProfile
    ): Call<ResponseBody>

    @GET("runners/home")
    fun getRunnerHome(
            @Query("minDate") minDate: ParamDate? = null,
            @Query("maxDate") maxDate: ParamDate? = null,
            @Query("page") page: Int? = null,
            @Query("limit") limit: Int? = null
    ): Call<BundleRunnerEventPage>

    @GET("runners/calendar")
    fun getRunnerCalendar(
            @Query("minDate") minDate: ParamDate? = null,
            @Query("maxDate") maxDate: ParamDate? = null,
            @Query("page") page: Int? = null,
            @Query("limit") limit: Int? = null,
            @Query("type") type: Int? = null
    ): Call<BundleRunnerEventPage>

    @GET("runners/{runnerId}/calendar")
    fun getRunnerCalendar(
            @Path("runnerId") runnerId: Int,
            @Query("minDate") minDate: ParamDate? = null,
            @Query("maxDate") maxDate: ParamDate? = null,
            @Query("page") page: Int? = null,
            @Query("limit") limit: Int? = null
    ): Call<BundleRunnerEventPage>

    @GET("runners/events/{eventId}")
    fun getRunnerEvent(
            @Path("eventId") eventId: Int
    ): Call<BundleEventAndRegistrations>

    @GET("runners/settings")
    fun getRunnerSettings(): Call<BundleRunnerSettings>

    @GET("runners/settings/departments")
    fun getRunnerSettingsDepartments(): Call<List<String>>

    @PUT("runners/settings/departments")
    fun putRunnerSettingsDepartments(
            @Body departments: List<String>
    ): Call<List<String>>

    @GET("runners/settings/raceTypes")
    fun getRunnerSettingsRaceTypes(): Call<List<String>>

    @PUT("runners/settings/raceTypes")
    fun putRunnerSettingsRaceTypes(
            @Body raceTypes: List<String>
    ): Call<List<String>>

    @GET("runners/settings/raceFormats")
    fun getRunnerSettingsRaceFormats(): Call<List<String>>

    @PUT("runners/settings/raceFormats")
    fun putRunnerSettingsRaceFormats(
            @Body raceFormats: List<String>
    ): Call<List<String>>

    @GET("runners/settings/notifications")
    fun getRunnerSettingsNotifications(): Call<BundleNotificationSettingsList>

    @PUT("runners/settings/notifications")
    fun putRunnerSettingsNotifications(
            @Body notifications: BundleNotificationSettingsList
    ): Call<BundleNotificationSettingsList>

    @GET("users/notifications")
    fun getUserNotificationList(
            @Query("page") page: Int? = null,
            @Query("limit") limit: Int? = null
    ): Call<BundleNotificationPage>

    @PUT("users/notifications/{notifId}/seen")
    fun putUserNotificationSeen(
            @Path("notifId") notifId: Int
    ): Call<ResponseBody>

    @GET("runners/invite")
    fun getRunnerInvite(
            @Query("runnerId") runnerId: Int,
            @Query("raceId") raceId: Int? = null
    ): Call<BundleDeepLinkData>

    @GET("friends")
    fun getFriendList(
            @Query("page") page: Int? = null,
            @Query("limit") limit: Int? = null
    ): Call<BundleFriendPage>

    @POST("friends")
    fun postFriend(
            @Body friendIds: BundleFriendId
    ): Call<ResponseBody>

    @GET("races/{raceId}/friends")
    fun getRaceFriendList(
            @Path("raceId") raceId: Int,
            @Query("page") page: Int? = null,
            @Query("limit") limit: Int? = null
    ): Call<BundleRaceFriendPage>

    @PUT("races/{raceId}/registrations")
    fun putRaceRegistration(
            @Path("raceId") raceId: Int,
            @Body registrationAccept: BundleRaceRegistrationAccept
    ): Call<ResponseBody>

    @GET("news")
    fun getNewsList(
            @Query("page") page: Int? = null,
            @Query("limit") limit: Int? = null
    ): Call<BundleNewsList>

    @GET("news/{slug}")
    fun getNewsArticle(
            @Path("slug") slug: String
    ): Call<BundleArticle>

    @GET("users/termsConsent")
    fun getUserTermsConsent(): Call<Boolean>

    @PUT("users/termsConsent")
    fun putUserTermsConsent(
            @Body consent: BundleUserTermsConsent
    ): Call<ResponseBody>

    @PUT("users/fcmToken")
    fun putFCMToken(
            @Body token: String?
    ): Call<ResponseBody>

}


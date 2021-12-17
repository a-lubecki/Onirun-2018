package com.onirun.screens.onboarding

import android.app.TaskStackBuilder
import android.content.Context
import android.os.Bundle
import com.onirun.R
import com.onirun.api.UserManager
import com.onirun.model.DeepLinkHandler
import com.onirun.model.DeepLinkType
import com.onirun.model.NotificationType
import com.onirun.screens.event.EventActivity
import com.onirun.screens.friends.FriendActivity
import com.onirun.screens.login.LoginActivity
import com.onirun.screens.login.LoginCause
import com.onirun.screens.login.SignUpConsentActivity
import com.onirun.screens.main.MainActivity
import com.onirun.screens.news.NewsArticleActivity
import com.onirun.screens.notification.NotificationClickManager
import com.onirun.screens.settings.SettingsLocationActivity
import com.onirun.screens.splash.ConfigureActivity
import com.onirun.utils.getSharedPreferences

object LaunchManager {


    private const val KEY_ONBOARDING_PASSED = "onboardingPassed"


    var deepLinkHandler: DeepLinkHandler? = null
    var bundleNotification: Bundle? = null

    var mustSendTermsConsent = false
    var hasAcceptedPushNotifs = false
    var hasAcceptedMailNotifs = false


    private fun setOnboardingPassed(context: Context) {
        context.getSharedPreferences().edit().putBoolean(KEY_ONBOARDING_PASSED, true).apply()
    }

    fun resetOnboardingPassed(context: Context) {
        context.getSharedPreferences().edit().remove(KEY_ONBOARDING_PASSED).apply()
    }

    fun isOnboardingPassed(context: Context): Boolean {
        return context.getSharedPreferences().getBoolean(KEY_ONBOARDING_PASSED, false)
    }


    fun tryStartDeepLinkActivity(context: Context) {

        when (LaunchManager.deepLinkHandler?.type) {

            DeepLinkType.ACCEPT_RACE -> {

                AcceptRaceActivity.start(context)
                return
            }

            DeepLinkType.ACCEPT_FRIEND -> {

                AcceptFriendActivity.start(context)
                return
            }

            DeepLinkType.EVENT -> {

                LaunchManager.deepLinkHandler?.eventId?.let {

                    TaskStackBuilder.create(context)
                            .addNextIntent(MainActivity.newIntentNewTask(context, R.id.calendar))
                            .addNextIntentWithParentStack(EventActivity.newIntent(context, it))
                            .startActivities()

                    return
                }
            }

            DeepLinkType.ARTICLE -> {

                LaunchManager.deepLinkHandler?.articleSlug?.let {

                    TaskStackBuilder.create(context)
                            .addNextIntent(MainActivity.newIntentNewTask(context, R.id.news))
                            .addNextIntentWithParentStack(NewsArticleActivity.newIntent(context, it))
                            .startActivities()

                    return
                }
            }

            else -> {
            }
        }

        //default action
        LaunchManager.startSignUpConsentOrSettingsActivity(context)
    }

    fun startSignUpConsentOrSettingsActivity(context: Context) {

        when {

            UserManager.isUserLogged() && !UserManager.hasAcceptedTerms(context) -> SignUpConsentActivity.start(context)

            !LaunchManager.isOnboardingPassed(context) -> SettingsLocationActivity.start(context, true, true)

            else -> LaunchManager.startMainActivity(context)
        }
    }

    fun startLoginActivityIfNotLogged(context: Context, cause: LoginCause) {

        when {

            !UserManager.isUserLogged() -> LoginActivity.start(context, cause)

            !UserManager.hasAcceptedTerms(context) -> SignUpConsentActivity.start(context)

            else -> ConfigureActivity.startNewTask(context, false)
        }
    }

    fun startMainActivity(context: Context) {

        //now we are sure that the onboarding has been seen
        setOnboardingPassed(context)

        if (bundleNotification != null) {

            //process push notif click

            val b = bundleNotification!!

            val done = NotificationClickManager.handleClick(
                    context,
                    true,
                    extractId(b, NotificationClickManager.KEY_NOTIFICATION_ID),
                    NotificationType.findNotificationType(extractValue(b, NotificationClickManager.KEY_NOTIFICATION_TYPE)),
                    mapOf(
                            NotificationClickManager.KEY_EVENT_ID to extractId(b, NotificationClickManager.KEY_EVENT_ID),
                            NotificationClickManager.KEY_FRIEND_ID to extractId(b, NotificationClickManager.KEY_FRIEND_ID),
                            NotificationClickManager.KEY_SLUG to extractValue(b, NotificationClickManager.KEY_SLUG)
                    )
            )

            if (done) {
                return
            }
        }

        //else start main activity by default
        var started = false

        LaunchManager.deepLinkHandler?.apply {

            if (hasAcceptedRace) {

                eventId?.let {

                    TaskStackBuilder.create(context)
                            .addNextIntent(MainActivity.newIntentNewTask(context, R.id.calendar))
                            .addNextIntentWithParentStack(EventActivity.newIntent(context, it))
                            .startActivities()

                    started = true
                }

            } else if (hasAcceptedFriend) {

                runnerId?.let {

                    TaskStackBuilder.create(context)
                            .addNextIntent(MainActivity.newIntentNewTask(context, R.id.calendar))
                            .addNextIntentWithParentStack(FriendActivity.newIntent(context, it))
                            .startActivities()

                    started = true
                }
            }
        }

        if (!started) {
            MainActivity.startNewTask(context)
        }
    }

    private fun extractValue(bundle: Bundle, key: String): String? {
        return bundle[key] as? String
    }

    private fun extractId(bundle: Bundle, key: String): Int? {
        return extractValue(bundle, key)?.toIntOrNull()
    }

}
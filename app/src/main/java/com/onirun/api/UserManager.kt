package com.onirun.api

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.mcxiaoke.koi.ext.toast
import com.mcxiaoke.koi.log.logd
import com.mcxiaoke.koi.log.loge
import com.onirun.screens.onboarding.LaunchManager
import com.onirun.screens.splash.SplashActivity
import com.onirun.utils.getSharedPreferences
import java.util.*

object UserManager {


    private const val KEY_RUNNER_ID = "runnerId"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_TERMS_ACCEPTED = "termsAccepted"
    private const val KEY_FCM_TOKEN = "fcmToken"


    var authToken: String? = null
        private set

    private var authTokenExpirationDate: Date? = null

    private var runnerId: Int? = null

    private var userName: String = ""

    private var termsAccepted: Boolean? = null


    fun isUserLogged(): Boolean {

        val user = FirebaseAuth.getInstance().currentUser ?: return false
        return !user.isAnonymous
    }

    fun resetLocalUserData(context: Context) {

        updateRunnerId(context, null)
        updateUserName(context, "")

        APIManager.getAPICacheFile(context).delete()

        //put a wrong date to force the token refresh with the isAuthTokenValid() call in retrieveAuthTokenOrLogout(...)
        authTokenExpirationDate = Date(0)
    }

    fun isAuthTokenValid(): Boolean {

        if (authToken.isNullOrEmpty()) {
            return false
        }

        if (authTokenExpirationDate != null) {
            return Date().time < authTokenExpirationDate!!.time
        }

        return true
    }

    fun retrieveAuthTokenOrLogout(context: Context, onSuccess: (() -> Unit)?, onError: (() -> Unit)?) {

        if (isAuthTokenValid()) {
            onSuccess?.invoke()
            return
        }

        //only refresh the token if it has been set before and is expired
        val mustRefreshToken = (authToken != null && !isAuthTokenValid())

        authToken = null
        authTokenExpirationDate = null

        try {

            FirebaseAuth.getInstance().currentUser!!.getIdToken(mustRefreshToken).addOnCompleteListener { task ->

                if (!task.isSuccessful || task.result == null) {
                    onError?.invoke()
                    return@addOnCompleteListener
                }

                authToken = task.result!!.token

                //save the expiration date (tokens expire after 1 hour) minus 5 minutes (60 * 5 = 300) to avoid calling the server with a good token and then the server calling firebase with the expired token
                authTokenExpirationDate = Date((task.result!!.expirationTimestamp - 300) * 1000)

                context.logd("Firebase auth token :\n$authToken\n => expiration : $authTokenExpirationDate")

                onSuccess?.invoke()
            }

        } catch (e: Exception) {

            loge(javaClass.name, "Can't retrieve token : logout", e)
            logout(context)
        }
    }

    fun retrieveRunner(context: Context, forceRefresh: Boolean, completion: (() -> Unit)?) {

        if (!forceRefresh && runnerId != null) {

            //already retrieved
            completion?.invoke()
            return
        }

        APIManager.call(
                context,
                {
                    it.getRunner()
                },
                true,
                !forceRefresh,
                {

                    updateRunnerId(context, it.runnerId)
                    updateUserName(context, it.userName)

                    completion?.invoke()

                },
                {

                    completion?.invoke()
                }
        )
    }

    fun logout(context: Context) {

        authToken = null
        FirebaseAuth.getInstance().signOut()

        updateRunnerId(context, null)
        updateUserName(context, "")
        updateTermsAccepted(context, false)

        APIManager.getAPICacheFile(context).delete()

        setRegisteredFCMToken(context, null)

        context.toast(com.onirun.R.string.error_message_logout)

        //reset the funnel
        LaunchManager.resetOnboardingPassed(context)
        SplashActivity.startNewTask(context)
    }


    fun hasRunnerId(context: Context): Boolean {

        retrieveLocalRunnerId(context)

        return runnerId != null
    }

    fun getRunnerIdOrLogout(context: Context): Int? {

        retrieveLocalRunnerId(context)

        if (runnerId == null) {
            logout(context)
            return null
        }

        return runnerId!!
    }

    private fun retrieveLocalRunnerId(context: Context) {

        if (runnerId != null) {
            //already set
            return
        }

        runnerId = context.getSharedPreferences().getInt(KEY_RUNNER_ID, -1)

        if (runnerId!! < 0) {
            runnerId = null
        }
    }

    fun updateRunnerId(context: Context, runnerId: Int?) {

        this.runnerId = runnerId

        if (runnerId == null) {
            context.getSharedPreferences().edit().remove(KEY_RUNNER_ID).apply()
        } else {
            context.getSharedPreferences().edit().putInt(KEY_RUNNER_ID, runnerId).apply()
        }
    }


    fun getUserName(context: Context): String {

        retrieveLocalUserName(context)

        return userName
    }

    private fun retrieveLocalUserName(context: Context) {

        if (userName.isNotEmpty()) {
            //already set
            return
        }

        userName = context.getSharedPreferences().getString(KEY_USER_NAME, "")!!
    }

    fun updateUserName(context: Context, userName: String) {

        this.userName = userName

        context.getSharedPreferences().edit().putString(KEY_USER_NAME, userName).apply()
    }

    fun hasAcceptedTerms(context: Context): Boolean {

        if (termsAccepted != null) {
            //already set
            return termsAccepted!!
        }

        termsAccepted = context.getSharedPreferences().getBoolean(KEY_TERMS_ACCEPTED, false)

        return termsAccepted!!
    }

    fun updateTermsAccepted(context: Context, termsAccepted: Boolean) {

        this.termsAccepted = termsAccepted

        context.getSharedPreferences().edit().putBoolean(KEY_TERMS_ACCEPTED, termsAccepted).apply()
    }


    fun registerFCMToken(context: Context, token: String?) {

        if (token.isNullOrEmpty()) {
            return
        }

        context.logd("TOKEN FCM : $token")

        if (!isAuthTokenValid()) {
            //can't send to the server if auh token is not retrieved
            return
        }

        if (getRegisteredFCMToken(context) == token) {
            //already registered to the server
            return
        }

        //send token to the server
        APIManager.call(
                context,
                {
                    it.putFCMToken(token)
                },
                true,
                false,
                {

                    //mark as sent, if an error occurs this method will
                    setRegisteredFCMToken(context, token)

                },
                null
        )

    }

    private fun getRegisteredFCMToken(context: Context): String? {

        return context.getSharedPreferences().getString(KEY_FCM_TOKEN, null)
    }

    private fun setRegisteredFCMToken(context: Context, token: String?) {

        if (token == null) {
            context.getSharedPreferences().edit().remove(KEY_FCM_TOKEN).apply()
        } else {
            context.getSharedPreferences().edit().putString(KEY_FCM_TOKEN, token).apply()
        }
    }


}
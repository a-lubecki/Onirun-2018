package com.onirun.api

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.mcxiaoke.koi.log.logw
import com.onirun.model.Configuration

class FirstInitManager(private val onSuccess: (() -> Unit), private val onError: (() -> Unit)) {


    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()!!
    }

    private var isRetrievingData = false
    private var hasRetrievedUser = false
    private var hasRetrievedTermsConsent = false
    private var hasRetrievedConfiguration = false


    fun tryInit(context: Context) {

        if (isRetrievingData) {
            finishOnError()
            return
        }

        isRetrievingData = true

        tryLoginAnonymous(context) {

            retrieveFirebaseToken(context) {

                retrieveRunnerIdFromServer(context) {
                    finishInit()
                }

                retrieveTermsConsent(context) {
                    finishInit()
                }
            }
        }

        tryRetrieveConfiguration(context) {

            finishInit()
        }
    }

    private fun tryLoginAnonymous(context: Context, onSuccess: (() -> Unit)) {

        if (auth.currentUser != null) {
            //already logged in to firebase
            onSuccess()
            return
        }

        //sign in anonymously with firebase
        auth.signInAnonymously()
                .addOnCompleteListener {

                    if (!it.isSuccessful) {

                        val errorCode = (it.exception as? FirebaseAuthException)?.errorCode ?: -1
                        val errorMessage = it.exception?.message ?: "-"

                        context.logw("Problem ($errorCode) : $errorMessage")

                        finishOnError()

                        return@addOnCompleteListener
                    }

                    if (auth.currentUser == null) {

                        context.logw("Problem : not logged after auth request")

                        finishOnError()

                        return@addOnCompleteListener
                    }

                    //once signed in, retrieve auth token from firebase
                    onSuccess()
                }
    }

    private fun retrieveFirebaseToken(context: Context, onSuccess: (() -> Unit)) {

        if (UserManager.isAuthTokenValid()) {
            //firebase token already retrieved
            onSuccess()
            return
        }

        UserManager.retrieveAuthTokenOrLogout(context, {
            onSuccess()
        }, {
            finishOnError()
        })

    }

    private fun retrieveRunnerIdFromServer(context: Context, onSuccess: (() -> Unit)) {

        if (UserManager.hasRunnerId(context)) {
            //user id already retrieved
            hasRetrievedUser = true

            onSuccess()
            return
        }

        UserManager.retrieveRunner(context, false) {

            if (!UserManager.hasRunnerId(context)) {
                finishOnError()
                return@retrieveRunner
            }

            hasRetrievedUser = true

            onSuccess()
        }
    }

    private fun retrieveTermsConsent(context: Context, onSuccess: (() -> Unit)) {

        if (!UserManager.isUserLogged() || UserManager.hasAcceptedTerms(context)) {
            //terms can't have been accepted because not logged or terms have already been accepted
            hasRetrievedTermsConsent = true

            onSuccess()
            return
        }

        APIManager.call(
                context,
                {
                    it.getUserTermsConsent()
                },
                true,
                false,
                { accepted ->

                    UserManager.updateTermsAccepted(context, accepted)

                    hasRetrievedTermsConsent = true

                    onSuccess()
                },
                {
                    finishOnError()
                }
        )
    }

    private fun tryRetrieveConfiguration(context: Context, onSuccess: (() -> Unit)) {

        if (Configuration.hasInstance()) {
            //already has the config
            hasRetrievedConfiguration = true

            onSuccess()
            return
        }

        APIManager.call(
                context,
                {
                    it.getConfiguration()
                },
                false,
                false,
                { bundle ->

                    Configuration.initInstance(Configuration(bundle))

                    hasRetrievedConfiguration = true

                    onSuccess()
                },
                {
                    finishOnError()
                }
        )

    }

    private fun finishInit() {

        if (!hasRetrievedUser) {
            return
        }

        if (!hasRetrievedTermsConsent) {
            return
        }

        if (!hasRetrievedConfiguration) {
            return
        }

        isRetrievingData = false

        onSuccess()
    }

    private fun finishOnError() {

        isRetrievingData = false

        onError()
    }

}
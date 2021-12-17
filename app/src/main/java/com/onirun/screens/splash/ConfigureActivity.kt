package com.onirun.screens.splash

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.FirstInitManager
import com.onirun.api.UserManager
import com.onirun.model.RaceEngagement
import com.onirun.model.bundle.BundleFriendId
import com.onirun.model.bundle.BundleRaceRegistrationAccept
import com.onirun.model.bundle.BundleUserTermsConsent
import com.onirun.screens.account.UserNameActivity
import com.onirun.screens.onboarding.LaunchManager
import com.onirun.utils.newTask
import com.onirun.utils.setVisible
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_configure.*


class ConfigureActivity : AppCompatActivity() {


    companion object {

        const val EXTRA_HAS_JUST_ACCEPTED_TERMS = "hasJustAcceptedTerms"

        fun startNewTask(context: Context, hasJustAcceptedTerms: Boolean) {

            context.newIntent<ConfigureActivity>()
                    .newTask()
                    .putExtra(EXTRA_HAS_JUST_ACCEPTED_TERMS, hasJustAcceptedTerms)
                    .start(context)
        }
    }


    private var hasFirstInit = false

    private var hasSentRaceAccept = false
    private var hasSentFriendAccept = false

    private var nbSentTried = 0

    private val hasJustAcceptedTerms by lazy {
        intent.getBooleanExtra(EXTRA_HAS_JUST_ACCEPTED_TERMS, false)
    }

    private fun mustSendRaceAccept(): Boolean {

        val handler = LaunchManager.deepLinkHandler ?: return false

        //send if the runner has accepted the challenge or not
        return handler.raceId != null
    }

    private fun mustSendFriendAccept(): Boolean {

        val handler = LaunchManager.deepLinkHandler ?: return false

        //send only if the friend has been accepted
        return handler.runnerId != null &&
                handler.hasAcceptedFriend
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_configure)

        buttonRetry.setVisible(false)

        buttonRetry.setOnClickListener {

            buttonRetry.setVisible(false)
            progressBarLoader.setVisible(true)

            tryFirstInit()
        }

        tryFirstInit()
    }

    private fun tryFirstInit() {

        FirstInitManager(
                {
                    hasFirstInit = true

                    trySendAcceptTerms {
                        trySendDeepLinkData {
                            startNextActivity()
                        }
                    }
                },
                {
                    onError()
                }
        ).tryInit(this)
    }

    private fun trySendAcceptTerms(completion: (() -> Unit)) {

        if (!LaunchManager.mustSendTermsConsent) {
            //nothing to send => done
            completion()
            return
        }

        val hasAcceptedPushNotifs = LaunchManager.hasAcceptedPushNotifs
        val hasAcceptedMailNotifs = LaunchManager.hasAcceptedMailNotifs

        APIManager.call(
                this,
                {
                    it.putUserTermsConsent(BundleUserTermsConsent(
                            true,
                            hasAcceptedPushNotifs,
                            hasAcceptedMailNotifs
                    ))
                },
                true,
                false,
                {
                    completion()
                },
                {
                    onError()
                }
        )

    }

    private fun trySendDeepLinkData(completion: (() -> Unit)) {

        if (nbSentTried >= 2) {
            //ignore sending if the user has retried too many times to avoid being blocked on this screen
            completion()
            return
        }

        nbSentTried++

        trySendRaceAccept {
            onDeepLinkDataPartSent(completion)
        }

        trySendFriendAccept {
            onDeepLinkDataPartSent(completion)
        }
    }

    private fun onDeepLinkDataPartSent(completion: (() -> Unit)) {

        //next action when all is sent
        if ((!mustSendRaceAccept() || hasSentRaceAccept) &&
                (!mustSendFriendAccept() || hasSentFriendAccept)) {
            completion()
        }
    }

    private fun trySendRaceAccept(onSuccess: (() -> Unit)) {

        if (!mustSendRaceAccept() || hasSentRaceAccept) {
            //sending not required
            onSuccess()
            return
        }

        val handler = LaunchManager.deepLinkHandler!!

        val engagement = if (handler.hasAcceptedRace) RaceEngagement.FULLY_ENGAGED else RaceEngagement.NOT_ENGAGED

        APIManager.call(
                this,
                {
                    it.putRaceRegistration(
                            handler.raceId!!,
                            BundleRaceRegistrationAccept(
                                    engagement,
                                    handler.runnerId
                            )
                    )
                },
                true,
                false,
                {
                    hasSentRaceAccept = true

                    onSuccess()
                },
                {

                    if (it?.httpCode == 409) {
                        //already added but the state is correct
                        onSuccess()
                    } else {
                        onError()
                    }
                }
        )
    }

    private fun trySendFriendAccept(onSuccess: (() -> Unit)) {

        if (!mustSendFriendAccept() || hasSentFriendAccept) {
            //sending not required
            return
        }

        val runnerId = UserManager.getRunnerIdOrLogout(this)
        if (runnerId == null || runnerId == LaunchManager.deepLinkHandler!!.runnerId!!) {
            //can't accept itself as a friend
            hasSentFriendAccept = true

            onSuccess()
            return
        }

        APIManager.call(
                this,
                {
                    it.postFriend(
                            BundleFriendId(
                                    runnerId,
                                    LaunchManager.deepLinkHandler!!.runnerId!!
                            )
                    )
                },
                true,
                false,
                {
                    hasSentFriendAccept = true

                    onSuccess()
                },
                {

                    if (it?.httpCode == 409) {
                        //already added in db
                        onSuccess()
                        return@call
                    }

                    onError()
                }
        )
    }

    private fun startNextActivity() {

        when {

            hasJustAcceptedTerms -> UserNameActivity.start(this, true, true)

            else -> LaunchManager.startMainActivity(this)
        }
    }

    private fun onError() {

        buttonRetry.setVisible(true)
        progressBarLoader.setVisible(false)
    }

}

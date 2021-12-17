package com.onirun.screens.onboarding

import android.os.Bundle
import com.onirun.api.APIManager
import com.onirun.api.UserManager
import com.onirun.model.DeepLinkData
import com.onirun.screens.main.BaseLoadingActivity

abstract class BaseAcceptActivity : BaseLoadingActivity() {


    protected lateinit var deepLinkData: DeepLinkData
        private set

    protected abstract val mustRetrieveRace: Boolean


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (LaunchManager.deepLinkHandler == null) {

            abortDeepLinking()
            return
        }

        val deepLinkHandler = LaunchManager.deepLinkHandler!!

        if (deepLinkHandler.runnerId == null) {

            abortDeepLinking()
            return
        }

        if (UserManager.hasRunnerId(this) && deepLinkHandler.runnerId == UserManager.getRunnerIdOrLogout(this)) {

            abortDeepLinking()
            return
        }

        if (mustRetrieveRace && deepLinkHandler.raceId == null) {

            abortDeepLinking()
            return
        }

        showLoaderDelayed()

        //retrieve race
        APIManager.call(
                this,
                {
                    it.getRunnerInvite(
                            deepLinkHandler.runnerId!!,
                            deepLinkHandler.raceId
                    )
                },
                true,
                true, //call with cache to avoid calling the server twice for the 2 screens
                {

                    deepLinkData = DeepLinkData(it)

                    if (mustRetrieveRace) {
                        if (deepLinkData.event == null || deepLinkData.race == null) {

                            abortDeepLinking()
                            return@call
                        }
                    }

                    hideLoader()

                    updateUI()
                },
                {
                    abortDeepLinking()
                }
        )

    }

    protected abstract fun updateUI()

    protected abstract fun startNextActivity()

    private fun abortDeepLinking() {

        finish()
        startNextActivity()
    }

}
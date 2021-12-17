package com.onirun.screens.splash

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.mcxiaoke.koi.ext.longToast
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.BuildConfig
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.FirstInitManager
import com.onirun.api.UserManager
import com.onirun.model.DeepLinkHandler
import com.onirun.screens.notification.NotificationClickManager
import com.onirun.screens.onboarding.LaunchManager
import com.onirun.screens.onboarding.OnboardingActivity
import com.onirun.utils.newTask
import com.onirun.utils.setVisible
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_splash_screen.*


class SplashActivity : AppCompatActivity() {


    companion object {

        fun startNewTask(context: Context) {

            context.newIntent<SplashActivity>()
                    .newTask()
                    .start(context)
        }
    }


    private var isRetrievingData = false
    private var hasFinishedTimer = false
    private var hasFirstInit = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)

        buttonRetry.setVisible(false)
        progressBarLoader.setVisible(false)

        buttonRetry.setOnClickListener {

            buttonRetry.setVisible(false)
            progressBarLoader.setVisible(true)

            tryFirstInit()
        }

        //process firebase dynamic link
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
                .addOnSuccessListener {

                    val uri = it?.link ?: return@addOnSuccessListener

                    LaunchManager.deepLinkHandler = DeepLinkHandler(uri)
                }

        //process notification click => put into LaunchManager
        intent?.extras?.let {

            //keep the bundle for notif data when the required "type" key is present
            if (it.containsKey(NotificationClickManager.KEY_NOTIFICATION_TYPE)) {
                LaunchManager.bundleNotification = it
            }
        }

        val handler = Handler()
        handler.postDelayed({

            hasFinishedTimer = true

            tryStartNextActivity()

        }, 2000)

        tryFirstInit()
    }

    override fun onBackPressed() {
        //disable the back button
    }

    private fun tryFirstInit() {

        isRetrievingData = true

        FirstInitManager(
                {
                    isRetrievingData = false
                    hasFirstInit = true

                    tryStartNextActivity()
                },
                {
                    isRetrievingData = false

                    showError()
                }
        ).tryInit(this)
    }

    private fun tryStartNextActivity() {

        //start next activity, only when the timer is finished + the user is logged
        if (!hasFinishedTimer) {
            return
        }

        if (isRetrievingData) {
            //show the loader if the timer is finished but the init requests are not finished
            progressBarLoader.setVisible(true)
        }

        if (!hasFirstInit) {
            return
        }

        //show alert for debug
        if (BuildConfig.CAN_DEBUG_API && !isFinishing) {

            AlertDialog.Builder(this)
                    .setTitle("DEBUG")
                    .setMessage("Activer le mode debug API ?\nChaque appel API sera affichés, peut ensuite être désactivé dans la home.")
                    .setNegativeButton("Non") { _, _ ->
                        APIManager.isAPILogAlertEnabled = false
                    }
                    .setPositiveButton("Oui") { _, _ ->
                        APIManager.isAPILogAlertEnabled = true
                    }
                    .setOnDismissListener {

                        startNextActivity()
                    }
                    .create()
                    .show()

        } else {

            startNextActivity()
        }
    }

    private fun startNextActivity() {

        finish()

        when {

            UserManager.isUserLogged() || LaunchManager.isOnboardingPassed(this) -> LaunchManager.tryStartDeepLinkActivity(this)

            else -> OnboardingActivity.startNewTask(this)
        }
    }

    private fun showError() {

        buttonRetry.setVisible(true)
        progressBarLoader.setVisible(false)

        longToast(R.string.error_message_default)
    }

}

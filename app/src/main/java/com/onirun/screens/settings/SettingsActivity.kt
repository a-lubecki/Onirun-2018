package com.onirun.screens.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.UserManager
import com.onirun.model.Configuration
import com.onirun.model.Runner
import com.onirun.model.RunnerSettings
import com.onirun.screens.account.ProfileActivity
import com.onirun.screens.account.UserNameActivity
import com.onirun.screens.friends.FriendsListActivity
import com.onirun.screens.login.LoginActivity
import com.onirun.screens.login.LoginCause
import com.onirun.screens.main.BaseAnimatedActivity
import com.onirun.screens.news.NewsArticleActivity
import com.onirun.screens.notification.SettingsNotificationActivity
import com.onirun.screens.web.WebActivity
import com.onirun.utils.alertDialog
import com.onirun.utils.setVisible
import com.onirun.utils.showIfActivityRunning
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_settings.*


/**
 * Created by Raven on 22/02/2018.
 */
class SettingsActivity : BaseAnimatedActivity() {


    companion object {

        fun start(context: Context) {

            context.newIntent<SettingsActivity>()
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_settings

    private var runner: Runner? = null
    private var runnerSettings: RunnerSettings? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        avatar.setOnClickListener {
            startUserNameActivity()
        }

        textViewUserName.setOnClickListener {
            startUserNameActivity()
        }

        buttonLogin.setOnClickListener {

            if (!UserManager.isUserLogged()) {
                LoginActivity.start(this, LoginCause.DEFAULT)
            }
        }

        buttonProfile.setOnClickListener {

            if (!UserManager.isUserLogged()) {
                LoginActivity.start(this, LoginCause.DEFAULT)
            } else {
                ProfileActivity.startForResultApiReload(this)
            }
        }

        layoutLocation.setOnClickListener {

            SettingsLocationActivity.startForResultApiReload(this)
        }

        layoutRaceType.setOnClickListener {

            SettingsRaceTypeActivity.startForResultApiReload(this)
        }

        layoutRaceFormat.setOnClickListener {

            SettingsRaceFormatActivity.startForResultApiReload(this)
        }

        layoutFriends.setOnClickListener {

            if (runner == null) {
                //if not logged, promote the login
                LoginActivity.start(this, LoginCause.FRIENDS)

            } else {

                FriendsListActivity.startForResultApiReload(this)
            }
        }

        textViewNotification.setOnClickListener {

            SettingsNotificationActivity.start(this)
        }

        textViewSuggest.setOnClickListener {

            WebActivity.startSuggest(this)
        }

        textViewComment.setOnClickListener {

            WebActivity.startComment(this)
        }

        textViewAbout.setOnClickListener {

            NewsArticleActivity.start(this, "faites-vous-confiance")
        }

        textViewTerms.setOnClickListener {

            WebActivity.startTerms(this)
        }

        textViewPrivacy.setOnClickListener {

            WebActivity.startPrivacy(this)
        }

        buttonLogout.setOnClickListener {

            alertDialog()
                    .setTitle(R.string.settings_logout)
                    .setMessage(R.string.settings_logout_confirm)
                    .setPositiveButton(android.R.string.yes) { _, _ ->

                        UserManager.logout(this)
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .showIfActivityRunning()
        }

    }

    override fun onResume() {
        super.onResume()

        if (runnerSettings == null) {

            scrollView.setVisible(false)

            retrieveData()
        }

        //the username may have changed
        updateUIUserName()
    }

    override fun onAPIReloadRequested(data: Intent?) {
        super.onAPIReloadRequested(data)

        //data will be retrieved in onResume
        runnerSettings = null
        runner = null
    }

    private fun startUserNameActivity() {

        if (UserManager.isUserLogged()) {
            UserNameActivity.start(this, false, false, textViewUserName.text.toString())
        }
    }

    private fun retrieveData() {

        if (!UserManager.isUserLogged()) {

            APIManager.call(
                    this,
                    {
                        it.getRunnerSettings()
                    },
                    true,
                    false,
                    {

                        runnerSettings = RunnerSettings(it)

                        updateUI()
                    },
                    {
                        finish()
                    }
            )

        } else {

            APIManager.call(
                    this,
                    {
                        it.getRunner()
                    },
                    true,
                    false,
                    {

                        runner = Runner(it)
                        runnerSettings = runner!!.settings

                        UserManager.updateRunnerId(this, runner!!.runnerId)
                        UserManager.updateUserName(this, runner!!.userName)

                        updateUI()
                    },
                    {
                        finish()
                    }
            )
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {

        scrollView.setVisible(true)

        val isLogged = (runner != null)

        textViewGrade.text = if (isLogged) runner!!.getGradeName() else getString(R.string.settings_promote_subtitle)

        buttonLogin.setVisible(!isLogged)
        buttonProfile.setVisible(isLogged)

        textViewInfoLocation.text = runnerSettings!!.departments.joinToString { it.getDisplayableCode() }
        textViewInfoRaceType.text = runnerSettings!!.raceTypes.size.toString() + "/" + Configuration.getInstance().raceTypes.size
        textViewInfoRaceFormat.text = runnerSettings!!.raceFormats.size.toString() + "/" + Configuration.getInstance().raceFormats.size
        textViewInfoFriends.text = if (isLogged) runner!!.nbFriends.toString() else "0"

        buttonLogout.setVisible(isLogged)

        updateUIUserName()
    }

    private fun updateUIUserName() {

        avatar.updateUI()

        textViewUserName.text = if (runner != null) UserManager.getUserName(this) else getString(R.string.settings_promote_title)
    }

}
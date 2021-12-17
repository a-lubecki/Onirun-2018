package com.onirun.screens.onboarding

import android.content.Context
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.screens.login.LoginCause
import com.onirun.utils.setVisible
import com.onirun.utils.start
import com.onirun.utils.trackEvent
import kotlinx.android.synthetic.main.activity_accept_friend.*


class AcceptFriendActivity : BaseAcceptActivity() {


    companion object {

        fun start(context: Context) {

            context.newIntent<AcceptFriendActivity>()
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_accept_friend

    override val mustRetrieveRace = false


    override fun onResume() {
        super.onResume()

        LaunchManager.deepLinkHandler!!.hasAcceptedFriend = false

        buttonYes.setImageResource(R.drawable.button_yes_off)
        buttonNo.setImageResource(R.drawable.button_no_off)
    }

    override fun updateUI() {

        if (deepLinkData.isFriend) {

            //friend already accepted
            startNextActivity()
            finish()

            return
        }

        layoutContent.setVisible(true)

        val userName = deepLinkData.runner.userName

        textViewUserName.text = userName
        textViewGrade.text = deepLinkData.runner.getGradeName()
        textViewQuestion.text = getString(R.string.deeplink_accept_friend, userName)

        avatar.setCustomName(userName)

        buttonYes.setOnClickListener {

            buttonYes.setImageResource(R.drawable.button_yes_active)

            LaunchManager.deepLinkHandler!!.hasAcceptedFriend = true

            startNextActivity()

            trackEvent("add_friend_accepted")
        }

        buttonNo.setOnClickListener {

            buttonNo.setImageResource(R.drawable.button_no_active)

            LaunchManager.deepLinkHandler!!.hasAcceptedFriend = false

            startNextActivity()

            trackEvent("add_friend_refused")
        }

    }

    override fun startNextActivity() {

        when {

            LaunchManager.deepLinkHandler!!.hasAcceptedFriend -> LaunchManager.startLoginActivityIfNotLogged(this, LoginCause.DEEP_LINK)

            else -> LaunchManager.startMainActivity(this)
        }
    }

}
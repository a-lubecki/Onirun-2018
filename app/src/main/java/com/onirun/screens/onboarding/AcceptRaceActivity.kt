package com.onirun.screens.onboarding

import android.annotation.SuppressLint
import android.content.Context
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.model.DeepLinkEvent
import com.onirun.model.RaceEngagement
import com.onirun.screens.login.LoginCause
import com.onirun.utils.setVisible
import com.onirun.utils.start
import com.onirun.utils.trackEvent
import kotlinx.android.synthetic.main.activity_accept_race.*
import java.text.SimpleDateFormat
import java.util.*

class AcceptRaceActivity : BaseAcceptActivity() {


    companion object {

        fun start(context: Context) {

            context.newIntent<AcceptRaceActivity>()
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_accept_race

    override val mustRetrieveRace = true


    override fun onResume() {
        super.onResume()

        LaunchManager.deepLinkHandler!!.hasAcceptedRace = false

        buttonYes.setImageResource(R.drawable.button_yes_off)
        buttonNo.setImageResource(R.drawable.button_no_off)
    }

    @SuppressLint("SetTextI18n")
    override fun updateUI() {

        val isEventStarted = deepLinkData.event?.dateBegin?.let {
            Date().time >= it.time
        } ?: false

        if (isEventStarted || RaceEngagement.isEngaged(deepLinkData.engagement)) {

            //race started or already accepted
            startNextActivity()
            finish()

            return
        }

        layoutContent.setVisible(true)

        val race = deepLinkData.race!!
        val event = deepLinkData.event!!

        textViewChallenge.setVisible(event.challenge)

        textViewType.text = race.type?.name ?: ""
        textViewTitle.text = event.name + "\n" + race.name
        textViewDate.text = getDisplayableDate(event) + " • " + event.city
        textViewQuestion.text = getString(R.string.deeplink_accept_race, deepLinkData.runner.userName)

        buttonYes.setOnClickListener {

            buttonYes.setImageResource(R.drawable.button_yes_active)

            LaunchManager.deepLinkHandler?.hasAcceptedRace = true

            //accept friend automatically if not yet friend
            if (!deepLinkData.isFriend) {
                LaunchManager.deepLinkHandler?.hasAcceptedFriend = true
            }

            startNextActivity()

            trackEvent("challenge_race_accepted", Bundle {
                race.raceId?.let { raceId ->
                    putInt("id_race", raceId)
                }
            })
        }

        buttonNo.setOnClickListener {

            buttonNo.setImageResource(R.drawable.button_no_active)

            LaunchManager.deepLinkHandler?.hasAcceptedRace = false

            startNextActivity()

            trackEvent("challenge_race_refused", Bundle {
                race.raceId?.let { raceId ->
                    putInt("id_race", raceId)
                }
            })
        }

    }

    private fun getDisplayableDate(event: DeepLinkEvent): String {

        val dateBegin = event.dateBegin ?: ""

        val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)
        return dateFormat.format(dateBegin).capitalize()
    }

    override fun startNextActivity() {

        when {

            LaunchManager.deepLinkHandler!!.hasAcceptedRace -> LaunchManager.startLoginActivityIfNotLogged(this, LoginCause.DEEP_LINK)

            else -> AcceptFriendActivity.start(this)
        }
    }

}
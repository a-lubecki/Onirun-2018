package com.onirun.screens.event

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.asString
import com.onirun.R
import com.onirun.api.UserManager
import com.onirun.model.Event
import com.onirun.model.Race
import com.onirun.model.RaceEngagement
import com.onirun.model.RaceRegistration
import com.onirun.recyclerview.RunnerEventListHandler
import com.onirun.screens.friends.RaceFriendsActivity
import com.onirun.screens.login.LoginActivity
import com.onirun.screens.login.LoginCause
import com.onirun.utils.setVisible
import com.onirun.utils.trackEvent
import kotlinx.android.synthetic.main.row_race.view.*

/**
 * Created by Aurelien Lubecki
 * on 16/04/2018.
 */
class RowRace(context: Context) : FrameLayout(context), View.OnClickListener {


    private lateinit var race: Race
    private var registration: RaceRegistration? = null

    init {

        inflate(context, R.layout.row_race, this)

        buttonFriendsNone.setOnClickListener(this)
        buttonFriendsSome.setOnClickListener(this)
        buttonEngage.setOnClickListener(this)
    }

    fun setRace(event: Event, race: Race, registration: RaceRegistration?) {

        this.race = race
        this.registration = registration

        var textTypeDistance = race.type.shortName
        race.getDisplayableLengthKm()?.let { lengthKm ->
            textTypeDistance += " - $lengthKm"
        }

        textViewTypeDistance.text = textTypeDistance

        textViewChallenge.setVisible(registration != null && registration.challenge)

        textViewTitle.text = race.name

        val details = arrayOf(
                getStringOrEmpty(R.string.race_description_start, race.getDisplayableStartTime(event.isDuringOneDay)),
                getStringOrEmpty(R.string.race_description_location, race.address),
                getStringOrEmpty(R.string.race_description_price, race.price)
        ).filter {
            it.isNotEmpty()
        }.asString("\n")

        textViewDetails.text = details
        textViewDetails.setVisible(details.isNotEmpty())

        RunnerEventListHandler.fillFormatsEmojis(layoutFormats, race.formats, 40)

        RunnerEventListHandler.manageLongDescription(race.description, textViewDescription, buttonSeeMore, 5)


        val nbFriends = registration?.nbFriends ?: 0
        val engagement = registration?.engagement ?: RaceEngagement.NOT_ENGAGED

        if (nbFriends <= 0) {

            buttonFriendsNone.setVisible(true)
            buttonFriendsSome.setVisible(false)

        } else {

            buttonFriendsNone.setVisible(false)
            buttonFriendsSome.setVisible(true)

            @SuppressLint("SetTextI18n")
            buttonFriendsSome.text = "+$nbFriends"
        }

        buttonEngage.setImageResource(when (engagement) {
            RaceEngagement.PARTIALLY_ENGAGED -> R.drawable.add_calendar_standby
            RaceEngagement.FULLY_ENGAGED -> R.drawable.add_calendar_on
            else -> R.drawable.add_calendar_off
        })

    }

    override fun onClick(v: View?) {

        val activity = (v!!.context as AppCompatActivity)

        if (v == buttonFriendsNone || v == buttonFriendsSome) {

            RaceFriendsActivity.startForResultApiReload(activity, race.eventId, race.raceId)

            activity.trackEvent("invite_friend_btn", Bundle {
                putInt("id_race", race.raceId)
            })

        } else if (v == buttonEngage) {

            if (!UserManager.isUserLogged()) {
                LoginActivity.start(activity, LoginCause.EVENTS)
            } else {
                RaceEngageDialogFragment.newFragment(race.raceId, registration?.engagement).show(activity.supportFragmentManager, "race_engagement")
            }

            activity.trackEvent("race_subscription_btn", Bundle {
                putInt("id_race", race.raceId)
            })
        }
    }

    private fun getStringOrEmpty(resId: Int, value: String?): String {

        if (value.isNullOrBlank()) {
            return ""
        }

        return context.getString(resId, value)
    }

}
@file:Suppress("UNCHECKED_CAST", "all")

package com.onirun.test

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.onirun.api.APIManager
import com.onirun.api.APIService
import com.onirun.api.UserManager
import com.onirun.model.Configuration
import com.onirun.utils.setVisible
import retrofit2.Call

class APITestActivity : Activity() {


    private var hasConfig = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = Button(this)
        b.text = "TEST LOG"

        b.setOnClickListener {

            b.setVisible(false)

            logAllAPIMethods()
        }

        val l = FrameLayout(this)

        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        l.addView(b, params)

        setContentView(l)
    }

    private fun logAllAPIMethods() {

        if (!hasConfig) {

            APIManager.call(
                    this,
                    {
                        it.getConfiguration()
                    },
                    false,
                    false,
                    { bundle ->

                        hasConfig = true

                        Configuration.initInstance(Configuration(bundle))

                        //retry
                        logAllAPIMethods()
                    },
                    null
            )

        } else if (!UserManager.isUserLogged()) {

            FirebaseAuth.getInstance().signInWithEmailAndPassword("aurel.lubecki@gmail.com", "azerty")
                    .addOnSuccessListener {

                        //retry
                        if (UserManager.isUserLogged()) {
                            logAllAPIMethods()
                        }
                    }
                    .addOnFailureListener {
                        finish()
                    }


        } else if (!UserManager.isAuthTokenValid()) {

            UserManager.retrieveAuthTokenOrLogout(this, {

                //retry
                logAllAPIMethods()

            }, {

                finish()
            })

        } else {

            val otherRunnerId = 15
            val eventId = 1749
            val raceId = 3055

            val calls = listOf<((APIService) -> (Call<*>))>(
                    { it.getRunner() },
                    { it.getRunnerProfile() },
                    { it.getRunnerHome() },
                    { it.getRunnerCalendar() },
                    { it.getRunnerEvent(eventId) },
                    { it.getRunnerSettings() },
                    { it.getRunnerSettingsDepartments() },
                    { it.getRunnerSettingsRaceTypes() },
                    { it.getRunnerSettingsRaceFormats() },
                    { it.getRunnerSettingsNotifications() },
                    { it.getUserNotificationList() },
                    { it.getRunnerInvite(otherRunnerId, raceId) },
                    { it.getFriendList() },
                    { it.getRaceFriendList(raceId) }
            )

            callAPI(calls, 0)
        }

    }

    private fun callAPI(calls: List<((APIService) -> (Call<*>))>, currentCall: Int) {

        if (currentCall >= calls.size) {
            return
        }

        APIManager.call(
                this,
                calls[currentCall] as ((APIService) -> (Call<Any>)),
                true,
                false,
                {
                    callAPI(calls, currentCall + 1)
                },
                {
                    callAPI(calls, currentCall + 1)
                }
        )

    }

}
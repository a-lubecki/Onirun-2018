package com.onirun.screens.onboarding

import android.content.Context
import android.os.Bundle
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.screens.login.LoginCause
import com.onirun.screens.main.BaseAnimatedActivity
import com.onirun.utils.newTask
import com.onirun.utils.setVisible
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_onboarding.*


class OnboardingActivity : BaseAnimatedActivity() {


    companion object {

        fun startNewTask(context: Context) {

            context.newIntent<OnboardingActivity>()
                    .newTask()
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_onboarding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        buttonNext.setOnClickListener {

            LaunchManager.tryStartDeepLinkActivity(this)
        }

        if (LaunchManager.deepLinkHandler != null) {

            //hide the sign in button for deep link because the sign in screen will be displayed after
            textViewSignIn.setVisible(false)
            buttonSignIn.setVisible(false)

        } else {

            textViewSignIn.setVisible(true)
            buttonSignIn.setVisible(true)

            buttonSignIn.setOnClickListener {
                LaunchManager.startLoginActivityIfNotLogged(this, LoginCause.DEFAULT)
            }
        }

    }


}
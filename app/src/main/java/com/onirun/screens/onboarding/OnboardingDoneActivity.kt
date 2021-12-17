package com.onirun.screens.onboarding

import android.content.Context
import android.os.Bundle
import android.os.Handler
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.screens.main.BaseAnimatedActivity
import com.onirun.utils.newTask
import com.onirun.utils.start

class OnboardingDoneActivity : BaseAnimatedActivity() {


    companion object {

        fun startNewTask(context: Context) {

            context.newIntent<OnboardingDoneActivity>()
                    .newTask()
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_onboarding_done


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({

            LaunchManager.startMainActivity(this)

        }, 3000)

    }

}
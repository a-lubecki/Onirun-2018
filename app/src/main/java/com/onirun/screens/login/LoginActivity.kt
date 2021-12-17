package com.onirun.screens.login

import android.content.Context
import android.os.Bundle
import android.view.View
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.utils.setVisible
import com.onirun.utils.start
import com.onirun.utils.trackEvent
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseLoginActivity() {


    companion object {

        private const val KEY_CAUSE = "cause"

        fun start(context: Context, cause: LoginCause) {

            context.newIntent<LoginActivity>()
                    .putExtra(KEY_CAUSE, cause.ordinal)
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_login

    private val cause by lazy {
        LoginCause.values()[intent.getIntExtra(KEY_CAUSE, 0)]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        textViewTitle.text = getString(when (cause) {

            LoginCause.DEEP_LINK -> R.string.login_title_deep_link

            LoginCause.FRIENDS -> R.string.login_title_friends

            LoginCause.EVENTS -> R.string.login_title_events

            LoginCause.NOTIFICATION -> R.string.login_title_notification

            else -> R.string.login_title_default
        })

        //TODO bug with emails and facebook login
        buttonFacebook.visibility = View.INVISIBLE

        buttonFacebook.setOnClickListener {

            loginViaFacebook()

            trackEvent("facebook_btn")
        }

        buttonGoogle.setOnClickListener {

            loginViaGoogle()

            trackEvent("google_btn")
        }

        buttonMail.setOnClickListener {

            LoginMailCheckActivity.start(this)

            trackEvent("email_btn")
        }

    }

}
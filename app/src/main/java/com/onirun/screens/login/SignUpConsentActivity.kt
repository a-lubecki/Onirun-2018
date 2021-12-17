package com.onirun.screens.login

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.mcxiaoke.koi.ext.newIntent
import com.mcxiaoke.koi.ext.toast
import com.onirun.R
import com.onirun.api.UserManager
import com.onirun.screens.main.BaseAnimatedActivity
import com.onirun.screens.onboarding.LaunchManager
import com.onirun.screens.splash.ConfigureActivity
import com.onirun.screens.web.WebActivity
import com.onirun.utils.start
import com.onirun.utils.trackEvent
import kotlinx.android.synthetic.main.activity_signup_consent.*


class SignUpConsentActivity : BaseAnimatedActivity() {


    companion object {

        private const val KEY_EMAIL = "email"

        fun start(context: Context, email: String? = null) {

            context.newIntent<SignUpConsentActivity>()
                    .putExtra(KEY_EMAIL, email)
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_signup_consent


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = getString(
                R.string.signup_consent_terms_subtitle,
                getString(R.string.signup_consent_terms_subtitle_part0),
                getString(R.string.signup_consent_terms_subtitle_part1)
        )

        textViewTermsSubtitle.text = SpannableString(text).also {

            addClickSpan(it, R.string.signup_consent_terms_subtitle_part0) {

                WebActivity.startTerms(this)
            }

            addClickSpan(it, R.string.signup_consent_terms_subtitle_part1) {

                WebActivity.startPrivacy(this)
            }
        }

        textViewTermsSubtitle.setOnClickListener {

            WebActivity.startTerms(this)
        }
    }

    private fun addClickSpan(text: SpannableString, subTextId: Int, onClick: () -> Unit) {

        val subText = getString(subTextId)
        val index = text.indexOf(subText)

        text.setSpan(
                object : ClickableSpan() {

                    override fun onClick(p0: View?) {
                        onClick()
                    }
                },
                index,
                index + subText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_login, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {

            R.id.login -> {

                acceptTerms()
                return true
            }

            android.R.id.home -> {

                tryLogoutBeforeFinishing()

                return super.onOptionsItemSelected(item)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        tryLogoutBeforeFinishing()
    }

    private fun tryLogoutBeforeFinishing() {

        if (!UserManager.isUserLogged()) {
            return
        }

        //logout to avoid being blocked on this screen if he doesn't want to accept the terms with its account
        UserManager.logout(this)
    }

    private fun acceptTerms() {

        if (!switchTerms.isChecked) {
            toast(getString(R.string.signup_consent_validate))
            return
        }

        //pass the result of switches to the next activity
        LaunchManager.mustSendTermsConsent = true
        LaunchManager.hasAcceptedPushNotifs = switchPush.isChecked
        LaunchManager.hasAcceptedMailNotifs = switchMail.isChecked

        //update locally to avoid retrieving the value from the server in ConfigureActivity
        UserManager.updateTermsAccepted(this, true)

        if (!UserManager.isUserLogged()) {
            MailSignUpActivity.start(this, intent.getStringExtra(KEY_EMAIL))
        } else {
            ConfigureActivity.startNewTask(this, true)
        }

        if (LaunchManager.hasAcceptedMailNotifs) {
            trackEvent("newsletter_subscription_btn")
        }
    }

}
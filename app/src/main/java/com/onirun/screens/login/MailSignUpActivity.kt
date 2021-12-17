package com.onirun.screens.login

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_mail_sign_up.*


class MailSignUpActivity : BaseLoginActivity() {


    companion object {

        private const val KEY_EMAIL = "email"

        fun start(context: Context, email: String) {

            context.newIntent<MailSignUpActivity>()
                    .putExtra(KEY_EMAIL, email)
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_mail_sign_up

    private lateinit var email: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        email = intent.getStringExtra(KEY_EMAIL)

        textViewEmail.text = email

        textViewPasswordAdvice.text = textPasswordAdvice
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_login, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.login) {

            signUpViaMail(email, editTextPassword.text?.toString())
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}
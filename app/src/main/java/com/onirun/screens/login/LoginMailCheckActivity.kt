package com.onirun.screens.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_login_mail_check.*


class LoginMailCheckActivity : BaseLoginActivity(), TextWatcher {


    companion object {

        fun start(context: Context, isAnimated: Boolean = true) {

            context.newIntent<LoginMailCheckActivity>()
                    .start(context, isAnimated)
        }
    }


    override val layoutId = R.layout.activity_login_mail_check


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //disable the next button when there are no text in edit text
        editTextEmail.addTextChangedListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (!editTextEmail.text.isNullOrBlank()) {
            menuInflater.inflate(R.menu.menu_next, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.next) {

            val email = editTextEmail.text?.toString()

            checkIfAccountExists(email) { isExisting ->

                if (isExisting) {
                    //sign in
                    MailSignInActivity.start(this, email!!)

                } else {
                    //sign up
                    SignUpConsentActivity.start(this, email!!)
                }
            }

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        //do nothing
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        //do nothing
    }

    override fun afterTextChanged(p0: Editable?) {

        //update the next button
        invalidateOptionsMenu()
    }

}
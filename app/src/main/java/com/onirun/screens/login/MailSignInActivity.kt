package com.onirun.screens.login

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuthException
import com.mcxiaoke.koi.ext.newIntent
import com.mcxiaoke.koi.ext.toast
import com.onirun.R
import com.onirun.utils.alertDialog
import com.onirun.utils.showIfActivityRunning
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_mail_sign_in.*


class MailSignInActivity : BaseLoginActivity() {


    companion object {

        private const val KEY_EMAIL = "email"

        fun start(context: Context, email: String) {

            context.newIntent<MailSignInActivity>()
                    .putExtra(KEY_EMAIL, email)
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_mail_sign_in

    private lateinit var email: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        email = intent.getStringExtra(KEY_EMAIL)

        textViewEmail.text = email

        buttonResetPassword.setOnClickListener {

            alertDialog()
                    .setTitle(R.string.login_reset_password)
                    .setMessage(getString(R.string.login_reset_password_message, email))
                    .setPositiveButton(android.R.string.yes) { _, _ ->

                        resetPassword(email)

                    }
                    .setNegativeButton(android.R.string.no, null)
                    .showIfActivityRunning()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_login, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.login) {

            signInViaMail(email, editTextPassword.text?.toString())
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun resetPassword(email: String) {

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {

                    toast(R.string.login_reset_password_done)

                }
                .addOnFailureListener {

                    val errorMessage = (it as? FirebaseAuthException)?.localizedMessage
                            ?: return@addOnFailureListener

                    alertDialog()
                            .setTitle(R.string.error_title_login)
                            .setMessage(errorMessage)
                            .setNegativeButton(android.R.string.ok, null)
                            .showIfActivityRunning()
                }
    }

}
package com.onirun.screens.account

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.widget.TextView
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.UserManager
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_username.*


class UserNameActivity : BaseAccountActivity(), TextWatcher {


    companion object {

        private const val KEY_OLD_USER_NAME = "oldUserName"

        fun start(context: Context, newTask: Boolean, isFinishingLogin: Boolean, currentUserName: String? = null) {

            newIntent(context, UserNameActivity::class.java, newTask, isFinishingLogin)
                    .putExtra(KEY_OLD_USER_NAME, currentUserName)
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_username

    private val oldUserName by lazy {
        intent.getStringExtra(KEY_OLD_USER_NAME) ?: UserManager.getUserName(this) ?: ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editTextUserName.setText(oldUserName, TextView.BufferType.NORMAL)

        editTextUserName.addTextChangedListener(this)

        //update save button
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (editTextUserName.text.isEmpty()) {
            return false
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun retrieveData() {
        //no username to retrieve
    }

    override fun sendData() {

        val newUserName = editTextUserName.text.toString()

        APIManager.call(
                this,
                {
                    it.putRunnerUserName(newUserName)
                },
                true,
                false,
                {

                    UserManager.updateUserName(this, newUserName)

                    onSendDataSuccess()
                },
                {
                    onSendDataError()
                }
        )

    }

    override fun startNextOnboardingActivity() {
        ProfileActivity.start(this, isFinishingLogin)
    }

    override fun afterTextChanged(p0: Editable?) {

        invalidateOptionsMenu()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        //do nothing
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        //do nothing
    }

}
package com.onirun.screens.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.onirun.R
import com.onirun.screens.main.BaseLoadingActivity
import com.onirun.utils.newTask
import com.onirun.utils.start


abstract class BaseAccountActivity : BaseLoadingActivity() {


    companion object {

        private const val KEY_IS_FINISHING_LOGIN = "isFinishingLogin"

        fun newIntent(context: Context, cls: Class<*>, newTask: Boolean, isFinishingLogin: Boolean): Intent {

            return Intent(context, cls)
                    .apply { if (newTask) newTask() }
                    .putExtra(KEY_IS_FINISHING_LOGIN, isFinishingLogin)
        }

        fun startInternal(context: Context, cls: Class<*>, newTask: Boolean, isFinishingLogin: Boolean) {

            newIntent(context, cls, newTask, isFinishingLogin)
                    .start(context)
        }
    }


    protected var isFinishingLogin = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isFinishingLogin = intent?.getBooleanExtra(KEY_IS_FINISHING_LOGIN, false) ?: false
    }

    override fun onResume() {
        super.onResume()

        retrieveData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.save) {
            saveData()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    abstract fun retrieveData()

    private fun saveData() {

        showLoaderDelayed()

        //show save button
        invalidateOptionsMenu()

        sendData()
    }

    abstract fun sendData()

    protected fun onSendDataSuccess() {

        if (isFinishingLogin) {
            //start next settings activity for the onboarding
            startNextOnboardingActivity()
            return
        }

        //else return to account screen
        setResultAPIReload()
        finish()
    }

    protected fun onSendDataError() {

        //show save button
        invalidateOptionsMenu()

        hideLoader()
    }

    abstract fun startNextOnboardingActivity()

}
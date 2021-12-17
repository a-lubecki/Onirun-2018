package com.onirun.screens.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import com.onirun.R
import com.onirun.model.Configuration
import com.onirun.screens.splash.SplashActivity
import com.onirun.utils.alertDialog
import com.onirun.utils.showIfActivityRunning


/**
 * Created by Aurelien Lubecki
 * on 01/05/2018.
 */
abstract class BaseActivity : AppCompatActivity() {


    protected abstract val layoutId: Int

    fun isActivityValid(): Boolean {
        return !isFinishing && !isDestroyed
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(layoutId)
        title = ""

        if (!Configuration.hasInstance() &&
                intent.action != Intent.ACTION_MAIN &&
                !intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
            //the app was restarted after being deleted by the OS, the splash screen was not seen, restart from the beginning to init the configuration
            finish()
            SplashActivity.startNewTask(this)
            return
        }

        findViewById<Toolbar>(R.id.toolbar)?.let {
            setSupportActionBar(it)
        }

        if (canGoBack()) {

            supportActionBar?.apply {
                setHomeButtonEnabled(true)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.close)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

        if (canGoBack()) {

            super.onBackPressed()

        } else {

            alertDialog()
                    .setMessage(R.string.quit_message)
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        super.onBackPressed()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .showIfActivityRunning()
        }
    }

    private fun canGoBack(): Boolean {

        if (intent == null) {
            return true
        }

        return !hasFlag(intent.flags, Intent.FLAG_ACTIVITY_NEW_TASK) ||
                !hasFlag(intent.flags, Intent.FLAG_ACTIVITY_CLEAR_TASK)

    }

    private fun hasFlag(flags: Int, flag: Int): Boolean {

        //compare bit to bit
        return flags and flag == flag
    }

    protected fun hideKeyboard() {

        currentFocus?.let {
            (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

}
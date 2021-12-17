package com.onirun.screens.main

import android.content.Intent
import com.onirun.BuildConfig


abstract class BaseAnimatedActivity : BaseActivity() {


    companion object Static {

        private const val BUNDLE_KEY_SKIP_OVERRIDE_PENDING_TRANSITION = "skip"
        private const val BUNDLE_KEY_ENTER_ANIM_RES_ID = "enter"
        private const val BUNDLE_KEY_EXIT_ANIM_RES_ID = "exit"

        const val REQUEST_CODE_API_RELOAD = 1863
        const val RESULT_CODE_RELOAD = 7710
    }


    fun startActivityWithoutAnimation(intent: Intent) {

        startActivityWithoutAnimation(intent, -1)
    }

    fun startActivityWithoutAnimation(intent: Intent, requestCode: Int) {

        intent.putExtra(BUNDLE_KEY_SKIP_OVERRIDE_PENDING_TRANSITION, true)

        startActivityForResult(intent, requestCode)
    }

    fun startActivityWithCustomAnimation(intent: Intent, enterAnim: Int, exitAnim: Int) {

        startActivityWithCustomAnimation(intent, -1, enterAnim, exitAnim)
    }

    fun startActivityWithCustomAnimation(intent: Intent, requestCode: Int, enterAnim: Int, exitAnim: Int) {

        intent.putExtra(BUNDLE_KEY_ENTER_ANIM_RES_ID, enterAnim)
        intent.putExtra(BUNDLE_KEY_EXIT_ANIM_RES_ID, exitAnim)

        startActivityForResult(intent, requestCode)
    }

    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)

        if (intent == null) {
            return
        }

        val skipOverridePendingTransition = intent.getBooleanExtra(BUNDLE_KEY_SKIP_OVERRIDE_PENDING_TRANSITION, false)

        if (skipOverridePendingTransition) {
            // Remove extra in case of intent forwarding to avoid influencing the next finish
            intent.removeExtra(BUNDLE_KEY_SKIP_OVERRIDE_PENDING_TRANSITION)
            return
        }

        var enterAnimResId = intent.getIntExtra(BUNDLE_KEY_ENTER_ANIM_RES_ID, -1)
        var exitAnimResId = intent.getIntExtra(BUNDLE_KEY_EXIT_ANIM_RES_ID, -1)

        if (enterAnimResId == -1) {
            enterAnimResId = BuildConfig.ANIM_START_ENTER
        }

        if (exitAnimResId == -1) {
            exitAnimResId = BuildConfig.ANIM_START_EXIT
        }

        overridePendingTransition(
                enterAnimResId,
                exitAnimResId
        )
    }

    fun setResultAPIReload() {

        setResult(RESULT_CODE_RELOAD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_API_RELOAD &&
                resultCode == RESULT_CODE_RELOAD) {

            onAPIReloadRequested(data)
        }
    }

    open fun onAPIReloadRequested(data: Intent?) {
        //override if necessary
    }

    fun finishWithoutAnimation() {

        intent.putExtra(BUNDLE_KEY_SKIP_OVERRIDE_PENDING_TRANSITION, true)

        finish()
    }

    override fun finish() {
        super.finish()

        val skipOverridePendingTransition = intent?.getBooleanExtra(BUNDLE_KEY_SKIP_OVERRIDE_PENDING_TRANSITION, false)
                ?: false

        if (skipOverridePendingTransition) {
            // Remove extra in case of intent forwarding to avoid influencing the next startActivity
            intent.removeExtra(BUNDLE_KEY_SKIP_OVERRIDE_PENDING_TRANSITION)
            return
        }

        overridePendingTransition(
                BuildConfig.ANIM_FINISH_ENTER,
                BuildConfig.ANIM_FINISH_EXIT
        )
    }

}



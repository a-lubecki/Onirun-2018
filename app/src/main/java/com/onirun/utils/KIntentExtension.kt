package com.onirun.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.onirun.screens.main.BaseAnimatedActivity

/**
 * Created by Aurelien Lubecki
 * on 28/05/2018.
 */

fun Intent.newTask(): Intent {
    return addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
}

fun Intent.start(context: Context, isAnimated: Boolean = true) {

    if (!isAnimated && context is BaseAnimatedActivity) {

        context.startActivityWithoutAnimation(this)

    } else {

        //default start
        context.startActivity(this)
    }
}

fun Intent.startForResult(activity: Activity, requestCode: Int, isAnimated: Boolean = true) {

    if (!isAnimated && activity is BaseAnimatedActivity) {

        activity.startActivityWithoutAnimation(this, requestCode)

    } else {

        //default start
        activity.startActivityForResult(this, requestCode)
    }
}
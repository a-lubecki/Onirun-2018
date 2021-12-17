package com.onirun.utils

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.onirun.R

/**
 * Created by Aurelien Lubecki
 * on 28/05/2018.
 */

fun Context.getSharedPreferences(): SharedPreferences {
    return getSharedPreferences("onirun_shared_prefs", 0)
}

fun Context.alertDialog(): AlertDialog.Builder {
    return AlertDialog.Builder(this, R.style.AppDialogTheme)
}

fun Context.trackEvent(name: String, params: Bundle? = null) {
    FirebaseAnalytics.getInstance(this).logEvent(name, params)
}
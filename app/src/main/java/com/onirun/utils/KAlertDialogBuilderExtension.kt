package com.onirun.utils

import android.app.AlertDialog
import android.view.ContextThemeWrapper
import com.onirun.screens.main.BaseActivity

/**
 * Created by Aurelien Lubecki
 * on 28/05/2018.
 */

fun AlertDialog.Builder.showIfActivityRunning() {

    //try to find the activity to know if not finishing
    var activity = context as? BaseActivity

    if (activity == null) {
        activity = (context as? ContextThemeWrapper)?.baseContext as? BaseActivity
    }

    if (activity != null && !activity.isActivityValid()) {
        //if activity was found, not ready to show dialog
        return
    }

    //show the dialog with a risk of crash if the base activity is finishing
    return create().show()
}

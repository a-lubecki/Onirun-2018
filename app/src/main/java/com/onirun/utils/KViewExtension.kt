package com.onirun.utils

import android.view.View

/**
 * Created by Aurelien Lubecki
 * on 24/03/2018.
 */

fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

fun View.setVisible(visible: Boolean) {

    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}


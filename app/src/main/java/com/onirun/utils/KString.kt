package com.onirun.utils

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.mcxiaoke.koi.log.logw


fun String.toBitmap(): Bitmap? {

    try {
        val imageBytes = Base64.decode(this, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    } catch (e: Exception) {
        logw("Unable to decode RaceFormat bitmap", e)
    }

    return null
    //void image if the src is incorrect
//    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
}

fun String.share(startActivityStrategy: ((Intent) -> Unit)) {

    Intent().setAction(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_TEXT, this)
            .setType("text/plain")
            .also { intent ->
                startActivityStrategy(intent)
            }
}


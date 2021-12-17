package com.onirun.utils

import android.os.Handler
import android.view.ViewGroup
import android.widget.ProgressBar

class LoadingContentHandler(
        private var layoutContent: ViewGroup? = null,
        private var loader: ProgressBar? = null) {


    private val handlerDelay = Handler()


    fun showLoaderDelayed() {

        layoutContent?.setVisible(false)

        handlerDelay.postDelayed({

            showLoader()
        }, 1000)
    }

    fun showLoader() {

        layoutContent?.setVisible(false)
        loader?.setVisible(true)
    }

    fun hideLoader() {

        handlerDelay.removeCallbacksAndMessages(null)

        //animate showing
        layoutContent?.apply {

            if (!isVisible()) {

                setVisible(true)

                alpha = 0f
                animate().alpha(1f)
            }
        }

        loader?.setVisible(false)
    }

}
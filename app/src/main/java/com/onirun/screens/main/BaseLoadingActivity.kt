package com.onirun.screens.main

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ProgressBar
import com.onirun.R
import com.onirun.utils.LoadingContentHandler

abstract class BaseLoadingActivity : BaseAnimatedActivity() {


    protected open val baseLayoutContent: ViewGroup? by lazy {
        findViewById<ViewGroup>(R.id.layoutContent)
    }

    protected open val baseLoader: ProgressBar? by lazy {
        findViewById<ProgressBar>(R.id.progressBarLoader)
    }

    protected val loadingContentHandler by lazy {
        LoadingContentHandler(baseLayoutContent, baseLoader)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideLoader()
    }

    protected fun showLoaderDelayed() {
        loadingContentHandler.showLoaderDelayed()
    }

    protected open fun showLoader() {
        loadingContentHandler.showLoader()
    }

    protected open fun hideLoader() {
        loadingContentHandler.hideLoader()
    }

}
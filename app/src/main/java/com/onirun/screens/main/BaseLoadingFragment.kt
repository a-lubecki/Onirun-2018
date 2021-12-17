package com.onirun.screens.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.onirun.R
import com.onirun.utils.LoadingContentHandler

/**
 * Created by Aurelien Lubecki
 * on 19/03/2018.
 */
abstract class BaseLoadingFragment : Fragment() {


    protected open val baseLayoutContent: ViewGroup? by lazy {
        view?.findViewById<ViewGroup>(R.id.layoutContent)
    }

    protected open val baseLoader: ProgressBar? by lazy {
        view?.findViewById<ProgressBar>(R.id.progressBarLoader)
    }

    protected val loadingContentHandler by lazy {
        LoadingContentHandler(baseLayoutContent, baseLoader)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
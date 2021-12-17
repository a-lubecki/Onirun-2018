package com.onirun.screens.web

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.BuildConfig
import com.onirun.R
import com.onirun.screens.main.BaseAnimatedActivity
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : BaseAnimatedActivity() {


    companion object {

        private const val KEY_TRACKING_TAG = "trackingTag"
        private const val KEY_URL = "url"

        private fun start(context: Context, trackingTag: String, url: String) {

            context.newIntent<WebActivity>()
                    .putExtra(KEY_TRACKING_TAG, trackingTag)
                    .putExtra(KEY_URL, url)
                    .start(context)
        }

        fun startSuggest(context: Context) {

            start(
                    context,
                    "suggest",
                    BuildConfig.ONIRUN_URL_API + "society/suggestions"
            )
        }

        fun startComment(context: Context) {

            start(
                    context,
                    "comment",
                    BuildConfig.ONIRUN_URL_API + "society/ask-us"
            )
        }

        fun startTerms(context: Context) {

            start(
                    context,
                    "terms",
                    BuildConfig.ONIRUN_URL_API + "society/cgu"
            )
        }

        fun startPrivacy(context: Context) {

            start(
                    context,
                    "privacy",
                    BuildConfig.ONIRUN_URL_API + "society/politique-confidentialite"
            )
        }

        fun startMoreDepartments(context: Context) {

            start(
                    context,
                    "department",
                    BuildConfig.ONIRUN_URL_API + "society/more-departments"
            )
        }
    }


    override val layoutId = R.layout.activity_web


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.extras?.getString(KEY_TRACKING_TAG)?.let { tag ->

            if (tag.isNotEmpty()) {
                //TODO track with tag
            }
        }

        //configure webview
        webView.settings.apply {
            javaScriptEnabled = true
            useWideViewPort = true
        }

        //load page
        intent?.extras?.getString(KEY_URL)?.let {
            webView.loadUrl(it)
        }

    }


}
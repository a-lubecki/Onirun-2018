package com.onirun.screens.friends

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.widget.Button
import com.github.nitrico.lastadapter.LastAdapter
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.onirun.BR
import com.onirun.BuildConfig
import com.onirun.R
import com.onirun.recyclerview.LazyLoadingActivityHandler
import com.onirun.recyclerview.LazyLoadingFooter
import com.onirun.recyclerview.LazyLoadingListHandlerListener
import com.onirun.recyclerview.TitleHeader
import com.onirun.screens.main.BaseLoadingActivity
import com.onirun.utils.alertDialog
import com.onirun.utils.setVisible
import com.onirun.utils.share
import com.onirun.utils.showIfActivityRunning


abstract class BaseFriendsListActivity : BaseLoadingActivity(), LazyLoadingListHandlerListener {


    protected lateinit var lazyLoadingHandler: LazyLoadingActivityHandler

    private val recyclerView by lazy {
        findViewById<RecyclerView>(R.id.recyclerView)
    }

    override val baseLayoutContent: RecyclerView? by lazy {
        findViewById<RecyclerView>(R.id.recyclerView)
    }

    private val buttonChallengeFriend by lazy {
        findViewById<Button>(R.id.buttonChallengeFriend)
    }

    protected abstract fun onAdapterCreated(adapter: LastAdapter)

    protected abstract fun newDeepLinkUri(): Uri?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val items = mutableListOf<Any>()
        val adapter = LastAdapter(items, BR.item)
                .into(recyclerView)
                .map<LazyLoadingFooter>(R.layout.row_loader)
                .map<TitleHeader>(R.layout.row_settings_header)

        onAdapterCreated(adapter)

        lazyLoadingHandler = LazyLoadingActivityHandler(
                items,
                recyclerView,
                adapter,
                loadingContentHandler
        )
        lazyLoadingHandler.listener = this

        buttonChallengeFriend.setOnClickListener {
            shareDeepLink()
        }

        lazyLoadingHandler.onCreate()
    }

    override fun onResume() {
        super.onResume()

        lazyLoadingHandler.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            //the user may have a new friend in the list now
            lazyLoadingHandler.onActivityResultReload()

            //reload the previous activity
            setResultAPIReload()
        }
    }

    private fun shareDeepLink() {

        showLoaderDelayed()

        val deepLinkUrl = newDeepLinkUri() ?: return

        //construct long url, to know more => https://firebase.google.com/docs/dynamic-links/create-manually
        val uri = Uri.Builder()
                .scheme("https")
                .authority(getString(R.string.FIREBASE_DYNAMIC_LINK_HOST))
                .appendQueryParameter("link", deepLinkUrl.toString())
                .appendQueryParameter("apn", packageName)
                .appendQueryParameter("ibi", BuildConfig.FIREBASE_IOS_PACKAGE)
                .appendQueryParameter("isi", BuildConfig.FIREBASE_IOS_APP_STORE_ID)
                .appendQueryParameter("ofl", BuildConfig.ONIRUN_URL_DESKTOP)
                .build()

        //generate short link with long link
        FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setLongLink(uri)
                .buildShortDynamicLink()
                .addOnSuccessListener { link ->

                    hideLoader()

                    getString(R.string.share_message_challenge, link.shortLink).share {
                        startActivityForResult(it, 0)
                    }
                }
                .addOnFailureListener {

                    hideLoader()

                    alertDialog()
                            .setTitle(R.string.error_title_default)
                            .setMessage(getString(R.string.error_link_generation))
                            .setNegativeButton(android.R.string.ok, null)
                            .showIfActivityRunning()
                }

    }

    override fun showLoader() {
        super.showLoader()

        buttonChallengeFriend.setVisible(false)
    }

    override fun hideLoader() {
        super.hideLoader()

        buttonChallengeFriend.setVisible(true)
    }

}
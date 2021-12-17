package com.onirun.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.ParamDate
import com.onirun.model.RunnerEvent
import com.onirun.recyclerview.RunnerEventListHandler
import com.onirun.recyclerview.RunnerEventListHandlerListener
import com.onirun.screens.main.BaseMenuFragment
import com.onirun.utils.setVisible
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.recyclerview_default.*
import java.util.*


/**
 * Created by Aurelien Lubecki
 * on 24/03/2018.
 */
class HomeFragment : BaseMenuFragment(), RunnerEventListHandlerListener {


    companion object {
        private const val MAX_RANDOM_ILLUSTRATIONS = 3
    }


    override val baseLayoutContent: ViewGroup? by lazy {
        recyclerView
    }

    private lateinit var lazyLoadingHandler: RunnerEventListHandler


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lazyLoadingHandler = RunnerEventListHandler(
                mutableListOf(),
                recyclerView,
                loadingContentHandler,
                this,
                buttonFilter
        )

        buttonFilter.setOnClickListener {

            activity?.let { activity ->
                HomeFilterDialogFragment().show(activity.supportFragmentManager, "filter_home")
            }
        }

        lazyLoadingHandler.onCreate()
    }

    override fun resume() {
        super.resume()

        layoutNoEvents.setVisible(false)

        lazyLoadingHandler.onResume()

        //load illustration if couldn't do it during API response
        loadIllustrationOnce()
    }

    override fun retrieveNextRunnerEvents(page: Int) {

        if (page <= 0 && imageViewIllustration.drawable == null) {
            //show illustration when the very first page data is loaded
            imageViewIllustration.setVisible(false)
        }

        APIManager.call(
                requireContext(),
                {
                    it.getRunnerHome(
                            ParamDate(RunnerEventListHandler.getCurrentDateWithoutTime()),
                            ParamDate(RunnerEventListHandler.getLastDayOfNextMonths(12)),
                            page,
                            30
                    )
                },
                true,
                false,
                {

                    lazyLoadingHandler.onRetrievingRunnerEventsSuccess(it)

                    if (lazyLoadingHandler.items.isEmpty()) {
                        layoutNoEvents.setVisible(true)
                    }

                    //load illustration image once, only on home
                    if (lazyLoadingHandler.isOnFirstPage()) {
                        loadIllustrationOnce()
                    }

                },
                {
                    lazyLoadingHandler.onRetrievingDataError()
                }
        )

    }

    private fun loadIllustrationOnce() {

        if (isDetached || imageViewIllustration == null) {
            //can't fill the imageview (crash)
            return
        }

        if (imageViewIllustration.drawable != null) {
            //already loaded image
            return
        }

        if (lazyLoadingHandler.isLoadingFromServer) {
            //currently retrieving data
            return
        }

        imageViewIllustration.setVisible(true)

        //get first runner event
        val firstEvent = lazyLoadingHandler.items.firstOrNull {
            it is RunnerEvent
        } as? RunnerEvent

        val firstRaceTypeTag = firstEvent?.raceTypes?.firstOrNull()?.tag

        val path = "raceTypes/" + if (firstRaceTypeTag.isNullOrEmpty()) {
            "404"
        } else {
            firstRaceTypeTag + "-" + Random().nextInt(MAX_RANDOM_ILLUSTRATIONS)
        } + ".jpg"

        Glide.with(this)
                .load(FirebaseStorage.getInstance().reference.child(path))
                .into(imageViewIllustration)
    }

}
package com.onirun.recyclerview

import android.support.v7.widget.RecyclerView
import com.github.nitrico.lastadapter.LastAdapter
import com.onirun.utils.LoadingContentHandler
import java.util.*

/**
 * Created by Aurelien Lubecki
 * on 01/05/2018.
 */
open class LazyLoadingActivityHandler(items: MutableList<Any>,
                                      recyclerView: RecyclerView,
                                      adapter: LastAdapter,
                                      loadingContentHandler: LoadingContentHandler?)
    : LazyLoadingListHandler(items, recyclerView, adapter, loadingContentHandler) {


    companion object {
        const val MIN_TIME_BEFORE_RELOAD_SEC = 600 //10 min
    }

    private var lastReloadDate: Date? = null


    fun onCreate() {

        tryRetrieveNextPage()
    }

    fun onResume() {

        if (currentPage >= 0 &&
                hasElements() &&
                lastReloadDate != null &&
                Date().time < lastReloadDate!!.time + MIN_TIME_BEFORE_RELOAD_SEC * 1000) {
            //too soon to reload
            return
        }

        //refresh data if no elements or too long elapsed time since last reload
        tryReloadData()
    }

    fun onActivityResultReload() {

        tryReloadData()
    }

    override fun onRetrievingDataSuccess(newItems: List<Any>, nbRetrievedItems: Int, totalPages: Int?) {

        if (!isLoadingFromServer) {
            //request cancelled
            return
        }

        lastReloadDate = Date()

        super.onRetrievingDataSuccess(newItems, nbRetrievedItems, totalPages)
    }

}

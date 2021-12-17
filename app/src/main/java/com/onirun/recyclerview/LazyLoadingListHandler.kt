package com.onirun.recyclerview

import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.github.nitrico.lastadapter.LastAdapter
import com.onirun.R
import com.onirun.utils.LoadingContentHandler

/**
 * Created by Aurelien Lubecki
 * on 01/05/2018.
 */
open class LazyLoadingListHandler(val items: MutableList<Any>,
                                  val recyclerView: RecyclerView,
                                  val adapter: LastAdapter,
                                  val loadingContentHandler: LoadingContentHandler?) {


    var isLoadingFromServer = false
        private set

    var lastSentPage = -1
        private set
    var currentPage = -1
        private set

    var currentTotalPages = -1
        private set

    var hasLoadedAllData = false
        private set

    //listener can be set after but it must be set
    lateinit var listener: LazyLoadingListHandlerListener

    private var swipeRefreshLayout: SwipeRefreshLayout? = null


    init {

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastCompletelyVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (lastCompletelyVisibleItemPosition == items.size - 1) {

                    tryRetrieveNextPage()

                }

            }
        })

        swipeRefreshLayout = (recyclerView.parent as? SwipeRefreshLayout)
        swipeRefreshLayout?.apply {

            //define pull to refresh behavior
            setColorSchemeResources(R.color.backgroundAlternative)
            setProgressBackgroundColorSchemeResource(R.color.backgroundTheme)

            //disable when there are no elements
            isEnabled = false

            setOnRefreshListener {

                if (items.isEmpty()) {
                    //no need to refresh if no elements
                    return@setOnRefreshListener
                }

                tryRetrievePage(0)
            }
        }

    }

    fun clearData() {

        items.clear()
        adapter.notifyDataSetChanged()

        isLoadingFromServer = false
        lastSentPage = -1
        currentPage = -1
        currentTotalPages = -1
        hasLoadedAllData = false
    }

    fun hasElements(): Boolean {
        return !items.isEmpty()
    }

    fun isLoadingFirstPage(): Boolean {
        return isLoadingFromServer && lastSentPage == 0
    }

    fun isOnFirstPage(): Boolean {
        return currentPage == 0
    }

    fun isRefreshing(): Boolean {
        return swipeRefreshLayout != null && swipeRefreshLayout!!.isRefreshing
    }

    fun tryReloadData() {

        if (isLoadingFromServer && lastSentPage == 0) {
            //already sending request for the first page
            return
        }

        hasLoadedAllData = false

        clearData()
        tryRetrievePage(0)
    }

    fun tryRetrieveNextPage() {

        //next page
        tryRetrievePage(currentPage + 1)
    }

    private fun tryRetrievePage(page: Int) {

        if (isLoadingFromServer) {
            //already sending request
            return
        }

        if (hasLoadedAllData && !isRefreshing()) {
            //no need to retrieve next page if already at the end
            return
        }

        isLoadingFromServer = true
        lastSentPage = page

        //show global loader when the first page is loading
        if (page <= 0) {

            removeLazyLoadingItem()

            if (!isRefreshing()) {
                //center loader appears if not refreshing
                loadingContentHandler?.showLoaderDelayed()
            }
        }

        listener.retrieveNextData(page)
    }

    open fun onRetrievingDataSuccess(newItems: List<Any>, nbRetrievedItems: Int, totalPages: Int?) {

        if (!isLoadingFromServer) {
            //request cancelled
            return
        }

        isLoadingFromServer = false

        //erase old pages to put the first page
        if (isRefreshing()) {
            items.clear()
            currentTotalPages = -1
            hasLoadedAllData = false
        }

        //hide the loaders when the data is retrieved
        loadingContentHandler?.hideLoader()
        removeLazyLoadingItem()
        swipeRefreshLayout?.isRefreshing = false

        currentPage = lastSentPage

        items.addAll(newItems)

        if (totalPages != null) {
            currentTotalPages = totalPages
        }

        //all loaded if the total has been provided in the response (more than zero) + current page didn't reached the total
        hasLoadedAllData = nbRetrievedItems <= 0 ||
                (currentTotalPages > 0 && currentPage >= currentTotalPages - 1)

        //add loader at the end of the list
        if (!hasLoadedAllData) {
            items.add(LazyLoadingFooter())
        }

        //enable pull to refresh only if some elements have been loaded
        swipeRefreshLayout?.apply {
            isEnabled = items.isNotEmpty()
        }

        adapter.notifyDataSetChanged()
    }

    fun onRetrievingDataError() {

        isLoadingFromServer = false

        //hide the loader when the data is retrieved
        loadingContentHandler?.hideLoader()
        removeLazyLoadingItem()
        swipeRefreshLayout?.isRefreshing = false

        adapter.notifyDataSetChanged()
    }

    private fun removeLazyLoadingItem() {

        items.removeAll {
            it is LazyLoadingFooter
        }
    }

}

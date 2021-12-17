package com.onirun.screens.friends

import android.app.Activity
import android.net.Uri
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.UserManager
import com.onirun.databinding.RowFriendRunnerBinding
import com.onirun.model.DeepLinkHandler
import com.onirun.model.FriendRunner
import com.onirun.recyclerview.LazyLoadingListHandlerListener
import com.onirun.recyclerview.TitleHeader
import com.onirun.utils.startForResult


class FriendsListActivity : BaseFriendsListActivity(), LazyLoadingListHandlerListener {


    companion object {

        fun startForResultApiReload(activity: Activity) {

            activity.newIntent<FriendsListActivity>()
                    .startForResult(activity, REQUEST_CODE_API_RELOAD)
        }
    }


    override val layoutId = R.layout.activity_friends_list


    override fun onAdapterCreated(adapter: LastAdapter) {

        adapter.map<FriendRunner>(Type<RowFriendRunnerBinding>(R.layout.row_friend_runner)
                .onClick {

                    val runner = it.binding.item ?: return@onClick

                    FriendActivity.startForResultApiReload(this, runner)
                }
        )

    }

    override fun newDeepLinkUri(): Uri? {

        val runnerId = UserManager.getRunnerIdOrLogout(this) ?: return null

        return DeepLinkHandler.newDeepLinkAcceptFriend(runnerId)
    }

    override fun retrieveNextData(page: Int) {

        APIManager.call(
                this,
                {
                    it.getFriendList(
                            page,
                            20
                    )
                },
                true,
                false,
                { b ->

                    hideLoader()

                    val newItems: MutableList<Any> = b.getFriends().asSequence().map {
                        FriendRunner(it)
                    }.toMutableList()

                    //get real nb items, before adding title
                    val nbItems = newItems.size

                    //add title
                    if (lazyLoadingHandler.isLoadingFirstPage()) {
                        newItems.add(0, TitleHeader(getString(R.string.friends_list_title)))
                    }

                    lazyLoadingHandler.onRetrievingDataSuccess(newItems, nbItems, b.totalPages)

                },
                {
                    finish()
                }
        )

    }

}
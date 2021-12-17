package com.onirun.screens.friends

import android.app.Activity
import android.net.Uri
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.UserManager
import com.onirun.databinding.RowRaceRunnerBinding
import com.onirun.model.DeepLinkHandler
import com.onirun.model.RaceEngagement
import com.onirun.model.RaceRunner
import com.onirun.recyclerview.LazyLoadingListHandlerListener
import com.onirun.recyclerview.TitleHeader
import com.onirun.screens.login.LoginActivity
import com.onirun.screens.login.LoginCause
import com.onirun.utils.startForResult


class RaceFriendsActivity : BaseFriendsListActivity(), LazyLoadingListHandlerListener {


    companion object {

        const val EXTRA_EVENT_ID = "eventId"
        const val EXTRA_RACE_ID = "raceId"

        fun startForResultApiReload(activity: Activity, eventId: Int, raceId: Int) {

            if (!UserManager.isUserLogged()) {
                LoginActivity.start(activity, LoginCause.FRIENDS)
                return
            }

            activity.newIntent<RaceFriendsActivity>()
                    .putExtra(EXTRA_EVENT_ID, eventId)
                    .putExtra(EXTRA_RACE_ID, raceId)
                    .startForResult(activity, REQUEST_CODE_API_RELOAD)
        }
    }


    override val layoutId = R.layout.activity_race_friends

    private val eventId by lazy {
        intent.getIntExtra(EXTRA_EVENT_ID, 0)
    }

    private val raceId by lazy {
        intent.getIntExtra(EXTRA_RACE_ID, 0)
    }

    override fun onAdapterCreated(adapter: LastAdapter) {

        adapter.map<RaceRunner>(Type<RowRaceRunnerBinding>(R.layout.row_race_runner)
                .onBind {

                    val runner = it.binding.item ?: return@onBind

                    it.binding.imageViewEngagement.setImageResource(when (runner.engagement) {
                        RaceEngagement.PARTIALLY_ENGAGED -> R.drawable.run_standby
                        RaceEngagement.FULLY_ENGAGED -> R.drawable.run_yes
                        else -> R.drawable.run_no
                    })
                }
                .onClick {

                    val runner = it.binding.item ?: return@onClick

                    FriendActivity.startForResultApiReload(this, runner)
                }
        )

    }

    override fun newDeepLinkUri(): Uri? {

        val runnerId = UserManager.getRunnerIdOrLogout(this) ?: return null

        return DeepLinkHandler.newDeepLinkAcceptRace(runnerId, eventId, raceId)
    }

    override fun retrieveNextData(page: Int) {

        APIManager.call(
                this,
                {
                    it.getRaceFriendList(
                            raceId,
                            page,
                            20
                    )
                },
                true,
                false,
                { b ->

                    hideLoader()

                    val newItems: MutableList<Any> = b.getFriends().asSequence().map {
                        RaceRunner(it)
                    }.toMutableList()

                    //get real nb items, before adding title
                    val nbItems = newItems.size

                    //add title
                    if (lazyLoadingHandler.isLoadingFirstPage()) {

                        if (newItems.isEmpty()) {
                            newItems.add(0, TitleHeader(getString(R.string.race_friends_title_noone)))
                        } else {
                            newItems.add(0, TitleHeader(getString(R.string.race_friends_title_someone)))
                        }
                    }

                    lazyLoadingHandler.onRetrievingDataSuccess(newItems, nbItems, b.totalPages)

                },
                {
                    finish()
                }
        )

    }


}
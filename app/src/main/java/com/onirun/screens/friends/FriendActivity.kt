package com.onirun.screens.friends

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.ParamDate
import com.onirun.model.BaseRunner
import com.onirun.model.Configuration
import com.onirun.model.RunnerGrade
import com.onirun.recyclerview.RunnerEventListHandler
import com.onirun.recyclerview.RunnerEventListHandlerListener
import com.onirun.screens.main.BaseLoadingActivity
import com.onirun.utils.setVisible
import com.onirun.utils.startForResult
import kotlinx.android.synthetic.main.activity_friend.*
import kotlinx.android.synthetic.main.recyclerview_default.*

class FriendActivity : BaseLoadingActivity(), RunnerEventListHandlerListener {


    companion object {

        const val EXTRA_HAS_ALL_DATA = "hasAllData"
        const val EXTRA_FRIEND_ID = "friendId"
        const val EXTRA_FRIEND_NAME = "friendName"
        const val EXTRA_FRIEND_GRADE_TAG = "friendGradeTag"

        fun newIntent(context: Context, friendId: Int): Intent {

            return context.newIntent<FriendActivity>()
                    .putExtra(EXTRA_HAS_ALL_DATA, false)
                    .putExtra(EXTRA_FRIEND_ID, friendId)
        }

        fun startForResultApiReload(activity: Activity, runner: BaseRunner) {

            activity.newIntent<FriendActivity>()
                    .putExtra(EXTRA_HAS_ALL_DATA, true)
                    .putExtra(EXTRA_FRIEND_ID, runner.runnerId)
                    .putExtra(EXTRA_FRIEND_NAME, runner.userName)
                    .apply {
                        runner.grade?.let {
                            putExtra(EXTRA_FRIEND_GRADE_TAG, it.tag)
                        }
                    }
                    .startForResult(activity, REQUEST_CODE_API_RELOAD)
        }
    }


    override val layoutId = R.layout.activity_friend

    private lateinit var lazyLoadingHandler: RunnerEventListHandler

    override val baseLayoutContent: ViewGroup? by lazy {
        recyclerView
    }

    private val hasAllData by lazy {
        intent.getBooleanExtra(EXTRA_HAS_ALL_DATA, false)
    }

    private val friendId by lazy {
        intent.getIntExtra(EXTRA_FRIEND_ID, 0)
    }

    private var friendName: String? = null

    private var friendGrade: RunnerGrade? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lazyLoadingHandler = RunnerEventListHandler(
                mutableListOf(),
                recyclerView,
                loadingContentHandler,
                this,
                null
        )

        if (hasAllData) {

            friendName = intent.getStringExtra(EXTRA_FRIEND_NAME)

            friendGrade = intent.getStringExtra(EXTRA_FRIEND_GRADE_TAG)?.let {
                Configuration.getInstance().findGrade(it)
            }

            endRetrieveData()

            return
        }

        //retrieve runner
        APIManager.call(
                this,
                {
                    it.getRunner(friendId)
                },
                true,
                false,
                {

                    friendName = it.userName
                    friendGrade = Configuration.getInstance().findGrade(it.grade)

                    endRetrieveData()
                },
                {

                    finish()
                }
        )

    }

    private fun endRetrieveData() {

        if (friendName != null) {
            avatar.setCustomName(friendName!!)
        }

        avatar.updateUI()

        textViewUserName.text = friendName

        friendGrade?.let {
            textViewGrade.text = it.name
        }

        lazyLoadingHandler.onCreate()
    }

    override fun onResume() {
        super.onResume()

        recyclerView.setVisible(true)
        layoutNoEvents.setVisible(false)

        lazyLoadingHandler.onResume()
    }

    override fun onAPIReloadRequested(data: Intent?) {
        super.onAPIReloadRequested(data)

        lazyLoadingHandler.onActivityResultReload()

        //reload the previous activity
        setResultAPIReload()
    }

    override fun retrieveNextRunnerEvents(page: Int) {

        APIManager.call(
                this,
                {
                    it.getRunnerCalendar(
                            friendId,
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

                },
                {
                    lazyLoadingHandler.onRetrievingDataError()
                }
        )

    }

}
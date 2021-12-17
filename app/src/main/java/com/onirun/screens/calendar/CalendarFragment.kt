package com.onirun.screens.calendar

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mcxiaoke.koi.ext.Bundle
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.ParamDate
import com.onirun.api.UserManager
import com.onirun.model.CalendarType
import com.onirun.recyclerview.RunnerEventListHandler
import com.onirun.recyclerview.RunnerEventListHandlerListener
import com.onirun.screens.friends.FriendsListActivity
import com.onirun.screens.login.LoginActivity
import com.onirun.screens.login.LoginCause
import com.onirun.screens.main.BaseMenuFragment
import com.onirun.utils.setVisible
import com.onirun.utils.trackEvent
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.recyclerview_default.*

/**
 * Created by Raven
 * on 22/02/2018.
 */
class CalendarFragment : BaseMenuFragment(), RunnerEventListHandlerListener {


    override val baseLayoutContent: ViewGroup? by lazy {
        recyclerView
    }

    private lateinit var lazyLoadingHandler: RunnerEventListHandler

    private var calendarType = CalendarType.OWNER


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
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

                CalendarFilterDialogFragment().apply {
                    setTargetFragment(this@CalendarFragment, 0)
                    show(activity.supportFragmentManager, "filter_calendar")
                }
            }
        }

        buttonLogin.setOnClickListener {

            context?.let { context ->

                LoginActivity.start(context, LoginCause.EVENTS)

                context.trackEvent("create_account_btn", Bundle {
                    putString("screen", "agenda")
                })
            }
        }

        //calendar is a logged in only feature
        if (UserManager.isUserLogged()) {
            lazyLoadingHandler.onCreate()
        }

        buttonFriends.setOnClickListener {

            val activity = context as? Activity ?: return@setOnClickListener

            if (!UserManager.isUserLogged()) {
                LoginActivity.start(activity, LoginCause.FRIENDS)
            } else {
                FriendsListActivity.startForResultApiReload(activity)
            }
        }

    }

    override fun resume() {
        super.resume()

        //calendar is a logged in only feature
        val isLogged = UserManager.isUserLogged()

        layoutMembersOnly.setVisible(!isLogged)
        recyclerView.setVisible(isLogged)
        layoutFilter.setVisible(isLogged)
        layoutNoEvents.setVisible(false)

        if (isLogged) {
            lazyLoadingHandler.onResume()
        }
    }

    fun updateCalendar(type: CalendarType) {

        if (calendarType == type) {
            //no need to update
            return
        }

        calendarType = type

        layoutNoEvents.setVisible(false)

        lazyLoadingHandler.clearData()
        lazyLoadingHandler.tryRetrieveNextPage()
    }

    override fun retrieveNextRunnerEvents(page: Int) {

        APIManager.call(
                requireContext(),
                {
                    it.getRunnerCalendar(
                            ParamDate(RunnerEventListHandler.getCurrentDateWithoutTime()),
                            ParamDate(RunnerEventListHandler.getLastDayOfNextMonths(12)),
                            page,
                            30,
                            calendarType.ordinal
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
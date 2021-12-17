package com.onirun.screens.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.google.firebase.iid.FirebaseInstanceId
import com.mcxiaoke.koi.ext.inflater
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.UserManager
import com.onirun.screens.calendar.CalendarFragment
import com.onirun.screens.home.HomeFragment
import com.onirun.screens.news.NewsFragment
import com.onirun.screens.notification.NotificationCenterFragment
import com.onirun.screens.onboarding.LaunchManager
import com.onirun.services.OnirunFCMService
import com.onirun.utils.newTask
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference


@SuppressLint("InflateParams")
class MainActivity : BaseAnimatedActivity(), BottomNavigationView.OnNavigationItemSelectedListener {


    companion object {

        private const val KEY_FIRST_ITEM_ID = "firstItemId"

        fun newIntentNewTask(context: Context, firstItemId: Int = R.id.home): Intent {

            return context.newIntent<MainActivity>()
                    .putExtra(KEY_FIRST_ITEM_ID, firstItemId)
                    .newTask()
        }

        fun startNewTask(context: Context, firstItemId: Int = R.id.home) {

            newIntentNewTask(context, firstItemId)
                    .start(context)
        }
    }


    private var fragmentsToReload = mutableSetOf<Int>()
    private var fragments = mutableMapOf<Int, Fragment>()

    private val viewNotifBadge by lazy {

        inflater.inflate(R.layout.notification_badge, null, false)
    }

    private val broadcastReceiverNotif by lazy {
        BroadcastReceiverNotif(this)
    }

    override val layoutId = R.layout.activity_main


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //deep link data previously sent => no need to keep the deep link object
        LaunchManager.deepLinkHandler = null

        bottomNavigation.setOnNavigationItemSelectedListener(this)
        bottomNavigation.selectedItemId = intent.getIntExtra(KEY_FIRST_ITEM_ID, R.id.home)

        if (!UserManager.isUserLogged()) {
            //not logged : the badge always appear appear first
            setNotifBadgeVisible(true)

        } else {
            //logged, retrieved the last notif to know if the badge must appear

            APIManager.call(
                    this,
                    {
                        it.getUserNotificationList(
                                0,
                                1
                        )
                    },
                    true,
                    false,
                    { b ->

                        //set the badge visible if the last notif was not marked as seen
                        if (b.getNotifications().firstOrNull()?.seen == false) {
                            setNotifBadgeVisible(true)
                        }
                    },
                    {
                        //ignore if something wrong
                    }
            )
        }

        //register for notification receiving
        registerReceiver(broadcastReceiverNotif, IntentFilter(OnirunFCMService.INTENT_FILTER_NOTIF_RECEIVED))

        //send the generated FCM token to the server if previous sending failed
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {

            val token = it.result?.token ?: return@addOnCompleteListener

            UserManager.registerFCMToken(this, token)
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        replaceFragment(item.itemId)

        if (item.itemId == R.id.notifications) {
            //notification tab is selected : the badge must disappear
            setNotifBadgeVisible(false)
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        //unregister for notification receiving
        unregisterReceiver(broadcastReceiverNotif)
    }

    override fun onAPIReloadRequested(data: Intent?) {
        super.onAPIReloadRequested(data)

        fragmentsToReload.add(R.id.home)
        fragmentsToReload.add(R.id.calendar)

        //if an event has changed : reload home and calendar
        if (bottomNavigation.selectedItemId == R.id.home || bottomNavigation.selectedItemId == R.id.calendar) {
            replaceFragment(bottomNavigation.selectedItemId)
        }
    }

    private fun replaceFragment(menuItemId: Int) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        val currentFragment = supportFragmentManager.primaryNavigationFragment
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment)
        }

        var f = getMainFragment(menuItemId)

        if (f == null) {

            //create new fragment
            val fragmentClassName = getFragmentName(menuItemId)

            f = Fragment.instantiate(this, fragmentClassName)
            fragments[menuItemId] = f

            fragmentsToReload.remove(menuItemId)

            fragmentTransaction.add(R.id.mainContainer, f, fragmentClassName)

        } else {

            fragmentTransaction.show(f)
        }

        fragmentTransaction.setPrimaryNavigationFragment(f)
        fragmentTransaction.setReorderingAllowed(true)

        fragmentTransaction.commitNowAllowingStateLoss()
    }

    private fun getMainFragment(menuItemId: Int): Fragment? {

        if (!fragmentsToReload.contains(menuItemId)) {

            //reuse an existing fragment if exist
            val existingFragment = fragments[menuItemId]
            if (existingFragment != null) {
                return existingFragment
            }
        }

        return null
    }

    private fun getFragmentName(menuItemId: Int): String {

        val fragmentClass = when (menuItemId) {

            R.id.home -> HomeFragment::class

            R.id.calendar -> CalendarFragment::class

            R.id.news -> NewsFragment::class

            R.id.notifications -> NotificationCenterFragment::class

            else -> throw IllegalStateException()
        }

        return fragmentClass.java.name
    }


    private fun isNotifBadgeVisible(): Boolean {

        return viewNotifBadge.parent != null
    }

    private fun setNotifBadgeVisible(visible: Boolean) {

        if (visible == isNotifBadgeVisible()) {
            //no change
            return
        }

        //add or remove the badge view from the nav menu
        ((bottomNavigation.getChildAt(0) as? BottomNavigationMenuView)?.getChildAt(3) as? BottomNavigationItemView)?.let {

            if (visible) {
                it.addView(viewNotifBadge)
            } else {
                it.removeView(viewNotifBadge)
            }
        }
    }


    class BroadcastReceiverNotif(activity: MainActivity) : BroadcastReceiver() {

        private val activityRef = WeakReference(activity)

        override fun onReceive(context: Context, intent: Intent) {

            val activity = activityRef.get() ?: return

            //if a notification is received when the app running, display the badge
            if (activity.bottomNavigation.selectedItemId != R.id.notifications) {

                activity.setNotifBadgeVisible(true)

                //refresh notifs on notif center menu click
                activity.fragmentsToReload.add(R.id.notifications)

            } else {

                //refresh notifs now
                activity.replaceFragment(R.id.notifications)
            }
        }

    }

}
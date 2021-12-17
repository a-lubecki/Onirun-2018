package com.onirun.screens.notification

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.find
import com.onirun.BR
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.UserManager
import com.onirun.databinding.RowNotificationBinding
import com.onirun.model.Notification
import com.onirun.recyclerview.LazyLoadingActivityHandler
import com.onirun.recyclerview.LazyLoadingFooter
import com.onirun.recyclerview.LazyLoadingListHandlerListener
import com.onirun.screens.login.LoginActivity
import com.onirun.screens.login.LoginCause
import com.onirun.screens.main.BaseMenuFragment
import com.onirun.utils.getSharedPreferences
import com.onirun.utils.setVisible
import com.onirun.utils.trackEvent
import kotlinx.android.synthetic.main.fragment_notification_center.*
import kotlinx.android.synthetic.main.recyclerview_default.*
import java.util.*
import java.util.regex.Pattern


class NotificationCenterFragment : BaseMenuFragment(), LazyLoadingListHandlerListener {


    companion object {

        private const val KEY_DEFAULT_NOTIF_DATE = "notificationDefaultDate"
        private const val KEY_DEFAULT_NOTIF_WELCOME_SEEN = "notificationDefaultWelcomeSeen"
    }


    override val baseLayoutContent: ViewGroup? by lazy {
        recyclerView
    }

    private lateinit var lazyLoadingHandler: LazyLoadingActivityHandler


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_notification_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillDefaultNotifs()

        val items = mutableListOf<Any>()
        val adapter = LastAdapter(items, BR.item)
                .into(recyclerView)
                .map<LazyLoadingFooter>(R.layout.row_loader)
                .map<Notification>(Type<RowNotificationBinding>(R.layout.row_notification)
                        .onBind {

                            val notif = it.binding.item ?: return@onBind

                            updateNotifRow(
                                    it.binding.root,
                                    notif.emoji,
                                    notif.dateAdd,
                                    notif.message,
                                    notif.seen
                            )
                        }
                        .onClick {

                            val notif = it.binding.item ?: return@onClick

                            val context = context ?: return@onClick

                            //show the next screen
                            NotificationClickManager.handleClick(
                                    context,
                                    false,
                                    notif.notifId,
                                    notif.type,
                                    notif.data
                            )

                            markNotifRowAsSeen(it.binding.root, true)
                        })

        lazyLoadingHandler = LazyLoadingActivityHandler(
                items,
                recyclerView,
                adapter,
                loadingContentHandler
        )
        lazyLoadingHandler.listener = this

        //notification is a logged in only feature
        if (UserManager.isUserLogged()) {
            lazyLoadingHandler.onCreate()
        }
    }

    private fun fillDefaultNotifs() {

        val context = context ?: return

        var lastDateLong = context.getSharedPreferences().getLong(KEY_DEFAULT_NOTIF_DATE, 0)
        if (lastDateLong <= 0) {
            //add the current time if not set yet
            val now = Date().time
            context.getSharedPreferences().edit().putLong(KEY_DEFAULT_NOTIF_DATE, now).apply()

            lastDateLong = now
        }

        val lastDate = Date(lastDateLong)

        updateNotifRow(
                layoutNotifLogin,
                "\uD83D\uDE00",
                lastDate,
                getString(R.string.notification_default_login),
                context.getSharedPreferences().getBoolean(KEY_DEFAULT_NOTIF_WELCOME_SEEN, false)
        )

        layoutNotifLogin.setOnClickListener {

            val c = getContext() ?: return@setOnClickListener

            LoginActivity.start(c, LoginCause.NOTIFICATION)

            //set as seen
            c.getSharedPreferences().edit().putBoolean(KEY_DEFAULT_NOTIF_WELCOME_SEEN, true).apply()

            markNotifRowAsSeen(layoutNotifLogin, true)

            c.trackEvent("create_account", Bundle {
                putString("screen", "notification")
            })
        }

        //the welcome notif is not clickable and seen by default
        updateNotifRow(
                layoutNotifWelcome,
                "\uD83D\uDE4C",
                lastDate,
                getString(R.string.notification_default_welcome),
                true
        )
    }

    override fun resume() {
        super.resume()

        if (UserManager.isUserLogged()) {

            setWelcomeNotificationsVisible(false)

            lazyLoadingHandler.onResume()

        } else {

            setWelcomeNotificationsVisible(true)
        }
    }

    override fun retrieveNextData(page: Int) {

        APIManager.call(
                requireContext(),
                {
                    it.getUserNotificationList(
                            page,
                            30
                    )
                },
                true,
                false,
                { b ->

                    val newItems = b.getNotifications().asSequence().map {
                        Notification(it)
                    }.toMutableList()

                    lazyLoadingHandler.onRetrievingDataSuccess(newItems, newItems.size, b.totalPages)

                    if (newItems.isEmpty()) {
                        setWelcomeNotificationsVisible(true)
                    }

                },
                {
                    lazyLoadingHandler.onRetrievingDataError()
                }
        )

    }

    private fun updateNotifRow(viewRow: View, emoji: String, dateAdd: Date, message: String, seen: Boolean) {

        viewRow.find<TextView>(R.id.textViewEmoji).text = emoji
        viewRow.find<TextView>(R.id.textViewDate).text = getNotificationTimeText(dateAdd)
        viewRow.find<TextView>(R.id.textViewMessage).text = markUsersAsBold(message)

        markNotifRowAsSeen(viewRow, seen)
    }

    private fun markNotifRowAsSeen(viewRow: View, seen: Boolean) {

        viewRow.setBackgroundColor(ContextCompat.getColor(viewRow.context, if (seen) android.R.color.transparent else R.color.backgroundAlternative))
    }

    /**
     * When a user appear with the character "@", make it bold, ex : @toto
     */
    private fun markUsersAsBold(text: CharSequence): CharSequence {

        val boldPlaces = mutableMapOf<Int, Int>()

        val p = Pattern.compile("@\\S+")
        val m = p.matcher(text)

        while (m.find()) {

            boldPlaces[m.start()] = m.end()
        }

        return SpannableStringBuilder(text)
                .apply {

                    boldPlaces.toList().forEach { elem ->

                        setSpan(
                                StyleSpan(android.graphics.Typeface.BOLD),
                                elem.first, //start
                                elem.second, //end
                                Spannable.SPAN_INCLUSIVE_INCLUSIVE
                        )
                    }
                }
    }

    private fun setWelcomeNotificationsVisible(visible: Boolean) {

        layoutNotifLogin.setVisible(visible && !UserManager.isUserLogged())

        layoutNotifWelcome.setVisible(visible)
    }

    private fun getNotificationTimeText(date: Date?): String {

        if (date == null) {
            return ""
        }

        val diffSec = (Date().time - date.time) / 1000f

        //now : less than 60
        if (diffSec < 60) {
            return getString(R.string.time_now)
        }

        //min : 60
        if (diffSec < 3600) {
            return getString(R.string.time_min, (diffSec / 60f).toInt())
        }

        //hour : 60 * 24
        if (diffSec < 86400) {
            return getString(R.string.time_hour, (diffSec / 3600f).toInt())
        }

        //day : 60 * 60 * 24
        return getString(R.string.time_day, (diffSec / 86400f).toInt())
    }

}
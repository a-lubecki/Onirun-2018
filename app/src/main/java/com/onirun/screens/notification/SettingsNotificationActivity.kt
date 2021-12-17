package com.onirun.screens.notification

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.widget.ProgressBar
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIService
import com.onirun.api.UserManager
import com.onirun.databinding.RowSettingsNotificationBinding
import com.onirun.model.NotificationChannel
import com.onirun.model.NotificationSettingsType
import com.onirun.model.NotificationType
import com.onirun.model.bundle.BundleNotificationSettings
import com.onirun.model.bundle.BundleNotificationSettingsList
import com.onirun.recyclerview.NotificationSettingsSection
import com.onirun.screens.login.LoginActivity
import com.onirun.screens.login.LoginCause
import com.onirun.screens.settings.BaseSettingsPartActivity
import com.onirun.utils.isVisible
import com.onirun.utils.setVisible
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_settings_notification.*
import kotlinx.android.synthetic.main.recyclerview_default.*
import retrofit2.Call


class SettingsNotificationActivity : BaseSettingsPartActivity<NotificationSettingsType, BundleNotificationSettingsList>() {


    companion object {

        fun start(context: Context) {

            context.newIntent<SettingsNotificationActivity>()
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_settings_notification

    override val baseRecyclerView: RecyclerView by lazy {
        recyclerView
    }

    override val baseLoader: ProgressBar by lazy {
        progressBarLoader
    }

    private val previousSelectedItems = mutableSetOf<NotificationSettingsType>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buttonLogin.setVisible(false)

        buttonLogin.setOnClickListener {

            LoginActivity.start(this, LoginCause.NOTIFICATION)
        }
    }

    override fun mustSelectMinOneItem(): Boolean {
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (baseRecyclerView.isVisible() && selectedItems != previousSelectedItems) {
            menuInflater.inflate(R.menu.menu_save, menu)
            return true
        }

        return true
    }

    override fun onAdapterCreated(adapter: LastAdapter) {

        adapter.map<NotificationSettingsSection>(Type<RowSettingsNotificationBinding>(R.layout.row_settings_notification)
                .onCreate {

                    onRowCreate(it.binding.switchSelectPush)
                    onRowCreate(it.binding.switchSelectMail)
                }
                .onBind {

                    val section = it.binding.item ?: return@onBind

                    it.binding.layoutPush.setVisible(section.hasPush)
                    onRowBind(NotificationSettingsType(NotificationChannel.PUSH, section.type), it.binding.switchSelectPush)

                    it.binding.layoutMail.setVisible(section.hasMail)
                    onRowBind(NotificationSettingsType(NotificationChannel.MAIL, section.type), it.binding.switchSelectMail)
                }
        )
    }

    override fun mapRetrievedBundleToSelectedItemsSet(bundle: BundleNotificationSettingsList): Set<NotificationSettingsType> {

        return NotificationType.values()
                .flatMap {
                    listOf(
                            NotificationSettingsType(NotificationChannel.PUSH, it) to bundle.getPushEnable(it),
                            NotificationSettingsType(NotificationChannel.MAIL, it) to bundle.getMailEnable(it)
                    )
                }
                .asSequence()
                .filter {
                    it.second == true
                }
                .map {
                    it.first
                }.toSet()
    }

    override fun getDataItemsToDisplay(bundle: BundleNotificationSettingsList): Collection<Any> {

        //retain elements to display save button only when data changed
        previousSelectedItems.clear()
        previousSelectedItems.addAll(selectedItems)

        //display button to log in
        val isLogged = UserManager.isUserLogged()
        buttonLogin.setVisible(!isLogged)

        val res = mutableListOf<Any>()

        if (isLogged) {

            res.add(
                    NotificationSettingsSection(
                            getString(R.string.settings_notif_races_title),
                            getString(R.string.settings_notif_races_subtitle),
                            NotificationType.RACES,
                            true,
                            true
                    )
            )

            res.add(
                    NotificationSettingsSection(
                            getString(R.string.settings_notif_friends_title),
                            getString(R.string.settings_notif_friends_subtitle),
                            NotificationType.FRIENDS,
                            true,
                            true
                    )
            )
        }

        res.add(
                NotificationSettingsSection(
                        getString(R.string.settings_notif_suggest_title),
                        getString(R.string.settings_notif_suggest_subtitle),
                        NotificationType.SUGGEST,
                        true,
                        isLogged
                )
        )

        res.add(
                NotificationSettingsSection(
                        getString(R.string.settings_notif_news_title),
                        getString(R.string.settings_notif_news_subtitle),
                        NotificationType.NEWS,
                        true,
                        isLogged
                )
        )

        return res
    }

    override fun getAPICallRetrieve(service: APIService): Call<BundleNotificationSettingsList> {

        return service.getRunnerSettingsNotifications()
    }

    override fun getAPICallSend(service: APIService): Call<BundleNotificationSettingsList> {

        val selectedPushTypes = selectedItems.asSequence().filter {
            it.channel == NotificationChannel.PUSH
        }.map {
            it.type
        }.toList()

        val selectedMailTypes = selectedItems.asSequence().filter {
            it.channel == NotificationChannel.MAIL
        }.map {
            it.type
        }.toList()

        return service.putRunnerSettingsNotifications(
                BundleNotificationSettingsList(
                        BundleNotificationSettings(
                                selectedPushTypes.contains(NotificationType.RACES),
                                selectedPushTypes.contains(NotificationType.FRIENDS),
                                selectedPushTypes.contains(NotificationType.SUGGEST),
                                selectedPushTypes.contains(NotificationType.NEWS)
                        ),
                        BundleNotificationSettings(
                                selectedMailTypes.contains(NotificationType.RACES),
                                selectedMailTypes.contains(NotificationType.FRIENDS),
                                selectedMailTypes.contains(NotificationType.SUGGEST),
                                selectedMailTypes.contains(NotificationType.NEWS)
                        )
                )
        )
    }

    override fun startNextOnboardingActivity() {
        //do nothing
    }

}

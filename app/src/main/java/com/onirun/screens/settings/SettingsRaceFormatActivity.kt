package com.onirun.screens.settings

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.widget.CompoundButton
import android.widget.ProgressBar
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIService
import com.onirun.databinding.RowRaceFormatBinding
import com.onirun.databinding.RowRaceFormatGroupBinding
import com.onirun.model.Configuration
import com.onirun.model.RaceFormat
import com.onirun.model.RaceFormatGroup
import com.onirun.recyclerview.TitleHeader
import com.onirun.screens.onboarding.OnboardingDoneActivity
import com.onirun.utils.startForResult
import kotlinx.android.synthetic.main.activity_settings_race_format.*
import kotlinx.android.synthetic.main.recyclerview_default.*
import retrofit2.Call

/**
 * Created by Raven on 22/02/2018.
 */
class SettingsRaceFormatActivity : BaseSettingsPartActivity<RaceFormat, List<String>>() {


    companion object {

        fun start(context: Context, isFinishingLogin: Boolean) {
            startInternal(context, SettingsRaceFormatActivity::class.java, false, isFinishingLogin)
        }

        fun startForResultApiReload(activity: Activity) {

            activity.newIntent<SettingsRaceFormatActivity>()
                    .startForResult(activity, REQUEST_CODE_API_RELOAD)
        }
    }


    override val layoutId = R.layout.activity_settings_race_format

    override val baseRecyclerView: RecyclerView by lazy {
        recyclerView
    }

    override val baseLoader: ProgressBar by lazy {
        progressBarLoader
    }

    override fun mustSelectMinOneItem(): Boolean {
        return false
    }

    override fun onAdapterCreated(adapter: LastAdapter) {

        adapter.map<RaceFormat>(
                Type<RowRaceFormatBinding>(R.layout.row_race_format)
                        .onCreate {

                            onRowCreate(it.binding.switchSelect)
                        }
                        .onBind {

                            val raceFormat = it.binding.item ?: return@onBind

                            onRowBind(raceFormat, it.binding.switchSelect)

                            it.binding.imageViewEmoji.setImageBitmap(raceFormat.emoji)
                        }
        ).map<RaceFormatGroup>(
                Type<RowRaceFormatGroupBinding>(R.layout.row_race_format_group)
                        .onCreate {

                            onRowCreate(it.binding.switchSelect)
                        }
                        .onBind {

                            val group = it.binding.item ?: return@onBind

                            val switchSelect = it.binding.switchSelect

                            //set the item in the switch tag to select it
                            switchSelect.tag = group

                            //check but don't trigger the listener
                            switchSelect.setOnCheckedChangeListener(null)
                            switchSelect.isChecked = selectedItems.containsAll(getAllItemsForGroup())
                            switchSelect.setOnCheckedChangeListener(this)
                        }
        )
    }

    private fun getAllItemsForGroup(): Collection<RaceFormat> {
        return items.asSequence().filter { it is RaceFormat }.map { it as RaceFormat }.toList()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCheckedChanged(switch: CompoundButton?, isChecked: Boolean) {

        //use the switch tag previously set in the binding method
        val group = switch?.tag as? RaceFormatGroup
        if (group == null) {

            //race format selected
            super.onCheckedChanged(switch, isChecked)

            //update all other items async
            Handler().post {
                recyclerView.adapter?.notifyDataSetChanged()
            }

            return
        }

        //group selected
        if (isChecked) {
            selectedItems.addAll(getAllItemsForGroup())
        } else {
            selectedItems.removeAll(getAllItemsForGroup())
        }

        //update all other items async
        Handler().post {
            recyclerView.adapter?.notifyDataSetChanged()
        }

        //show or hide save button
        invalidateOptionsMenu()
    }

    override fun mapRetrievedBundleToSelectedItemsSet(bundle: List<String>): Set<RaceFormat> {

        return bundle.asSequence().mapNotNull {
            Configuration.getInstance().findRaceFormat(it)
        }.toMutableSet()
    }

    override fun getDataItemsToDisplay(bundle: List<String>): Collection<Any> {

        return listOf(
                TitleHeader(getString(R.string.settings_race_format_title_header)),
                RaceFormatGroup.DEFAULT
        ).plus(Configuration.getInstance().raceFormats.values)
    }

    override fun getAPICallRetrieve(service: APIService): Call<List<String>> {

        return service.getRunnerSettingsRaceFormats()
    }

    override fun getAPICallSend(service: APIService): Call<List<String>> {

        return service.putRunnerSettingsRaceFormats(
                selectedItems.asSequence().map {
                    it.tag
                }.toList()
        )
    }

    override fun startNextOnboardingActivity() {
        OnboardingDoneActivity.startNewTask(this)
    }

}
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
import com.onirun.databinding.RowRaceTypeBinding
import com.onirun.databinding.RowRaceTypeGroupBinding
import com.onirun.model.Configuration
import com.onirun.model.RaceType
import com.onirun.model.RaceTypeGroup
import com.onirun.recyclerview.SectionHeader
import com.onirun.recyclerview.TitleHeader
import com.onirun.utils.startForResult
import kotlinx.android.synthetic.main.activity_settings_race_type.*
import kotlinx.android.synthetic.main.recyclerview_default.*
import retrofit2.Call

/**
 * Created by Raven on 22/02/2018.
 */
class SettingsRaceTypeActivity : BaseSettingsPartActivity<RaceType, List<String>>() {


    companion object {

        fun start(context: Context, isFinishingLogin: Boolean) {
            startInternal(context, SettingsRaceTypeActivity::class.java, false, isFinishingLogin)
        }

        fun startForResultApiReload(activity: Activity) {

            activity.newIntent<SettingsRaceTypeActivity>()
                    .startForResult(activity, REQUEST_CODE_API_RELOAD)
        }
    }


    override val layoutId = R.layout.activity_settings_race_type

    override val baseRecyclerView: RecyclerView by lazy {
        recyclerView
    }

    override val baseLoader: ProgressBar by lazy {
        progressBarLoader
    }

    override fun mustSelectMinOneItem(): Boolean {
        return true
    }

    override fun onAdapterCreated(adapter: LastAdapter) {

        adapter.map<RaceType>(
                Type<RowRaceTypeBinding>(R.layout.row_race_type)
                        .onCreate {

                            onRowCreate(it.binding.switchSelect)
                        }
                        .onBind {

                            onRowBind(it.binding.item, it.binding.switchSelect)
                        }
        ).map<RaceTypeGroup>(
                Type<RowRaceTypeGroupBinding>(R.layout.row_race_type_group)
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
                            switchSelect.isChecked = selectedItems.containsAll(getAllItemsForGroup(group))
                            switchSelect.setOnCheckedChangeListener(this)
                        }
        )
    }

    private fun getAllItemsForGroup(group: RaceTypeGroup): Collection<RaceType> {
        return items.asSequence().filter { it is RaceType }.map { it as RaceType }.filter { it.group == group }.toList()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCheckedChanged(switch: CompoundButton?, isChecked: Boolean) {

        //use the switch tag previously set in the binding method
        val group = switch?.tag as? RaceTypeGroup
        if (group == null) {

            //race type selected
            super.onCheckedChanged(switch, isChecked)

            //update all other items async
            Handler().post {
                recyclerView.adapter?.notifyDataSetChanged()
            }

            return
        }

        //group selected
        if (isChecked) {
            selectedItems.addAll(getAllItemsForGroup(group))
        } else {
            selectedItems.removeAll(getAllItemsForGroup(group))
        }

        //update all other items async
        Handler().post {
            recyclerView.adapter?.notifyDataSetChanged()
        }

        //show or hide save button
        invalidateOptionsMenu()
    }

    override fun mapRetrievedBundleToSelectedItemsSet(bundle: List<String>): Set<RaceType> {

        return bundle.asSequence().mapNotNull {
            Configuration.getInstance().findRaceType(it)
        }.toMutableSet()
    }

    override fun getDataItemsToDisplay(bundle: List<String>): Collection<Any> {

        val sections = mapOf<RaceTypeGroup, MutableList<RaceType>>(
                RaceTypeGroup.ROAD to mutableListOf(),
                RaceTypeGroup.NATURE to mutableListOf(),
                RaceTypeGroup.MULTI_SPORT to mutableListOf()
        )

        Configuration.getInstance().raceTypes.values.forEach {
            sections[it.group]?.add(it)
        }

        return sequenceOf(
                TitleHeader(getString(R.string.settings_race_type_title_header))
        ).plus(SectionHeader(getString(R.string.settings_race_type_section_road)))
                .plus(RaceTypeGroup.ROAD)
                .plus(sections.getValue(RaceTypeGroup.ROAD))
                .plus(SectionHeader(getString(R.string.settings_race_type_section_nature)))
                .plus(RaceTypeGroup.NATURE)
                .plus(sections.getValue(RaceTypeGroup.NATURE))
                .plus(SectionHeader(getString(R.string.settings_race_type_section_multisport)))
                .plus(RaceTypeGroup.MULTI_SPORT)
                .plus(sections.getValue(RaceTypeGroup.MULTI_SPORT))
                .toList()

    }

    override fun getAPICallRetrieve(service: APIService): Call<List<String>> {

        return service.getRunnerSettingsRaceTypes()
    }

    override fun getAPICallSend(service: APIService): Call<List<String>> {

        return service.putRunnerSettingsRaceTypes(
                selectedItems.asSequence().map {
                    it.tag
                }.toList()
        )
    }

    override fun startNextOnboardingActivity() {
        SettingsRaceFormatActivity.start(this, isFinishingLogin)
    }

}
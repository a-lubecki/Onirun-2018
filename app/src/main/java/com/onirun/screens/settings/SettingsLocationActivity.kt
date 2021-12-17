package com.onirun.screens.settings

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.widget.ProgressBar
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIService
import com.onirun.databinding.RowDepartmentBinding
import com.onirun.model.Configuration
import com.onirun.model.Department
import com.onirun.recyclerview.SectionHeader
import com.onirun.recyclerview.TitleHeader
import com.onirun.screens.web.WebActivity
import com.onirun.utils.setVisible
import com.onirun.utils.startForResult
import kotlinx.android.synthetic.main.activity_settings_location.*
import kotlinx.android.synthetic.main.recyclerview_default.*
import retrofit2.Call

/**
 * Created by Raven on 22/02/2018.
 */
class SettingsLocationActivity : BaseSettingsPartActivity<Department, List<String>>() {


    companion object {

        fun start(context: Context, newTask: Boolean, isFinishingLogin: Boolean) {
            startInternal(context, SettingsLocationActivity::class.java, newTask, isFinishingLogin)
        }

        fun startForResultApiReload(activity: Activity) {

            activity.newIntent<SettingsLocationActivity>()
                    .startForResult(activity, REQUEST_CODE_API_RELOAD)
        }
    }


    override val layoutId = R.layout.activity_settings_location

    override val baseRecyclerView: RecyclerView by lazy {
        recyclerView
    }

    override val baseLoader: ProgressBar by lazy {
        progressBarLoader
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateBritainOnly()

    }

    //TODO remove this method when not necessary any more
    private fun updateBritainOnly() {

        //in the beginning display a message if there are only the 4 departments of britain
        val departmentCodes = Configuration.getInstance().departments.keys

        val isBritainOnly = departmentCodes.minus(arrayOf("22", "29", "35", "56")).isEmpty()

        buttonBritainOnly.setVisible(isBritainOnly)
        textViewBritainOnly.setVisible(isBritainOnly)

        if (isBritainOnly) {

            buttonBritainOnly.setOnClickListener {
                WebActivity.startMoreDepartments(this)
            }
        }
    }

    override fun mustSelectMinOneItem(): Boolean {
        return true
    }

    override fun onAdapterCreated(adapter: LastAdapter) {

        adapter.map<Department>(
                Type<RowDepartmentBinding>(R.layout.row_department)
                        .onCreate {

                            onRowCreate(it.binding.switchSelect)
                        }
                        .onBind {

                            onRowBind(it.binding.item, it.binding.switchSelect)
                        }
        )
    }

    override fun mapRetrievedBundleToSelectedItemsSet(bundle: List<String>): Set<Department> {

        return bundle.asSequence().mapNotNull {
            Configuration.getInstance().findDepartment(it)
        }.toMutableSet()
    }

    override fun getDataItemsToDisplay(bundle: List<String>): Collection<Any> {

        return listOf(
                TitleHeader(getString(R.string.settings_location_title_header)),
                SectionHeader(getString(R.string.settings_location_section_header))
        ).plus(Configuration.getInstance().departments.values)
    }

    override fun getAPICallRetrieve(service: APIService): Call<List<String>> {

        return service.getRunnerSettingsDepartments()
    }

    override fun getAPICallSend(service: APIService): Call<List<String>> {

        return service.putRunnerSettingsDepartments(
                selectedItems.asSequence().map {
                    it.code
                }.toList()
        )
    }

    override fun startNextOnboardingActivity() {
        SettingsRaceTypeActivity.start(this, isFinishingLogin)
    }

}
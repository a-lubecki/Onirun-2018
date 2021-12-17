package com.onirun.screens.settings

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.view.Menu
import android.view.ViewGroup
import android.widget.CompoundButton
import com.github.nitrico.lastadapter.BR
import com.github.nitrico.lastadapter.LastAdapter
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.api.APIService
import com.onirun.recyclerview.SectionHeader
import com.onirun.recyclerview.TitleHeader
import com.onirun.screens.account.BaseAccountActivity
import com.onirun.screens.onboarding.LaunchManager
import com.onirun.utils.isVisible
import retrofit2.Call

abstract class BaseSettingsPartActivity<ItemType, BundleType> : BaseAccountActivity(), CompoundButton.OnCheckedChangeListener {


    protected abstract val baseRecyclerView: RecyclerView

    override val baseLayoutContent: ViewGroup by lazy {
        baseRecyclerView
    }

    protected val items = mutableListOf<Any>()
    protected var selectedItems = mutableSetOf<ItemType>()
        private set

    private lateinit var adapter: LastAdapter

    protected abstract fun mustSelectMinOneItem(): Boolean
    protected abstract fun onAdapterCreated(adapter: LastAdapter)
    protected abstract fun mapRetrievedBundleToSelectedItemsSet(bundle: BundleType): Set<ItemType>
    protected abstract fun getDataItemsToDisplay(bundle: BundleType): Collection<Any>
    protected abstract fun getAPICallRetrieve(service: APIService): Call<BundleType>
    protected abstract fun getAPICallSend(service: APIService): Call<BundleType>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = LastAdapter(items, BR.item)
                .into(baseRecyclerView)
                .map<TitleHeader>(R.layout.row_settings_header)
                .map<SectionHeader>(R.layout.row_settings_section_header)

        onAdapterCreated(adapter)
    }

    override fun retrieveData() {

        showLoaderDelayed()

        selectedItems.clear()

        APIManager.call(
                this,
                {
                    getAPICallRetrieve(it)
                },
                true,
                false,
                {

                    hideLoader()

                    selectedItems.clear()
                    selectedItems.addAll(mapRetrievedBundleToSelectedItemsSet(it))

                    initData(it)
                },
                {
                    finish()
                }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        if (baseRecyclerView.isVisible() && items.isNotEmpty()) {

            if (mustSelectMinOneItem()) {

                //must select at least one item to save
                if (selectedItems.isNotEmpty()) {
                    return super.onCreateOptionsMenu(menu)
                }

            } else {
                //can ignore this screen
                if (selectedItems.isEmpty() && !LaunchManager.isOnboardingPassed(this)) {
                    menuInflater.inflate(R.menu.menu_ignore, menu)
                } else {
                    return super.onCreateOptionsMenu(menu)
                }
            }
        }

        return false
    }

    private fun initData(bundle: BundleType) {

        //init save button
        invalidateOptionsMenu()

        items.clear()
        items.addAll(getDataItemsToDisplay(bundle))

        adapter.notifyDataSetChanged()
    }

    override fun sendData() {

        APIManager.call(
                this,
                {
                    getAPICallSend(it)
                },
                true,
                false,
                {

                    onSendDataSuccess()
                },
                {

                    onSendDataError()
                }
        )

    }

    protected fun onRowCreate(switchSelect: SwitchCompat) {

        switchSelect.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(switch: CompoundButton?, isChecked: Boolean) {

        //use the switch tag previously set in the binding method
        @Suppress("UNCHECKED_CAST")
        val item = switch?.tag as? ItemType ?: return

        if (isChecked) {
            selectedItems.add(item)
        } else {
            selectedItems.remove(item)
        }

        //show or hide save button
        invalidateOptionsMenu()
    }

    protected fun onRowBind(item: ItemType?, switchSelect: SwitchCompat) {

        if (item == null) {
            return
        }

        //set the item in the switch tag to select it
        switchSelect.tag = item

        //check but don't trigger the listener
        switchSelect.setOnCheckedChangeListener(null)
        switchSelect.isChecked = selectedItems.contains(item)
        switchSelect.setOnCheckedChangeListener(this)
    }

}
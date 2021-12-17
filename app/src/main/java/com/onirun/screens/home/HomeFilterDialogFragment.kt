package com.onirun.screens.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.model.Configuration
import com.onirun.model.RunnerSettings
import com.onirun.screens.settings.SettingsLocationActivity
import com.onirun.screens.settings.SettingsRaceFormatActivity
import com.onirun.screens.settings.SettingsRaceTypeActivity
import kotlinx.android.synthetic.main.fragment_home_filter.*


class HomeFilterDialogFragment : BottomSheetDialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //start with activity anim
        layoutLocation.setOnClickListener {

            activity?.let { a ->
                SettingsLocationActivity.startForResultApiReload(a)
            }

            dismiss()
        }

        layoutRaceType.setOnClickListener {

            //start with activity anim
            activity?.let { a ->
                SettingsRaceTypeActivity.startForResultApiReload(a)
            }

            dismiss()
        }

        layoutRaceFormat.setOnClickListener {

            //start with activity anim
            activity?.let { a ->
                SettingsRaceFormatActivity.startForResultApiReload(a)
            }

            dismiss()
        }

        retrieveSettings()
    }

    @SuppressLint("SetTextI18n")
    private fun retrieveSettings() {

        APIManager.call(
                requireContext(),
                {
                    it.getRunnerSettings()
                },
                true,
                false,
                {

                    if (!isVisible || isRemoving) {
                        //fragment is closing
                        return@call
                    }

                    val runnerSettings = RunnerSettings(it)

                    textViewInfoLocation.text = runnerSettings.departments.joinToString { d ->
                        d.getDisplayableCode()
                    }

                    textViewInfoRaceType.text = runnerSettings.raceTypes.size.toString() + "/" + Configuration.getInstance().raceTypes.size

                    textViewInfoRaceFormat.text = runnerSettings.raceFormats.size.toString() + "/" + Configuration.getInstance().raceFormats.size
                },
                null
        )
    }

}
package com.onirun.screens.event

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mcxiaoke.koi.ext.Bundle
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.model.RaceEngagement
import com.onirun.model.bundle.BundleRaceRegistrationAccept
import com.onirun.utils.setVisible
import com.onirun.utils.trackEvent
import kotlinx.android.synthetic.main.fragment_race_engage.*


class RaceEngageDialogFragment : BottomSheetDialogFragment() {


    companion object {

        const val EXTRA_RACE_ID = "raceId"
        const val EXTRA_ENGAGEMENT = "currentEngagement"

        fun newFragment(raceId: Int, currentEngagement: RaceEngagement?): RaceEngageDialogFragment {

            return RaceEngageDialogFragment()
                    .apply {
                        arguments = Bundle {
                            putInt(EXTRA_RACE_ID, raceId)
                            putInt(EXTRA_ENGAGEMENT, currentEngagement?.ordinal ?: 0)
                        }
                    }
        }
    }


    private val raceId: Int by lazy {
        arguments!!.getInt(EXTRA_RACE_ID)
    }

    private val currentEngagement: RaceEngagement by lazy {
        RaceEngagement.getEngagement(arguments!!.getInt(EXTRA_ENGAGEMENT))
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_race_engage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutEngage2.setOnClickListener {
            sendEngagement(RaceEngagement.FULLY_ENGAGED)
        }

        layoutEngage1.setOnClickListener {
            sendEngagement(RaceEngagement.PARTIALLY_ENGAGED)
        }

        layoutEngage0.setOnClickListener {
            sendEngagement(RaceEngagement.NOT_ENGAGED)
        }

    }

    private fun sendEngagement(engagement: RaceEngagement) {

        if (currentEngagement == engagement) {
            //same engagement is selected
            dismiss()
            return
        }

        hideContent()

        APIManager.call(
                requireContext(),
                {
                    it.putRaceRegistration(
                            raceId,
                            BundleRaceRegistrationAccept(engagement)
                    )
                },
                true,
                false,
                {

                    dismiss()

                    val activity = (activity as? EventActivity) ?: return@call

                    activity.updateEvent()

                    activity.trackEvent("add_to_calendar_btn", Bundle {
                        putString("motivation", engagement.trackingTag)
                        putInt("id_race", raceId)
                    })

                },
                {
                    showContent()
                }
        )

    }

    private fun showContent() {
        layoutContent.setVisible(true)
    }

    private fun hideContent() {
        layoutContent.setVisible(false)
    }

}
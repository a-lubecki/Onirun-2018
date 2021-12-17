package com.onirun.screens.calendar

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.onirun.R
import com.onirun.model.CalendarType
import kotlinx.android.synthetic.main.fragment_calendar_filter.*


class CalendarFilterDialogFragment : BottomSheetDialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutOwner.setOnClickListener {
            selectType(CalendarType.OWNER)
        }

        layoutFriends.setOnClickListener {
            selectType(CalendarType.FRIENDS)
        }

        layoutOwnerAndFriends.setOnClickListener {
            selectType(CalendarType.OWNER_AND_FRIENDS)
        }

    }

    private fun selectType(type: CalendarType) {

        (targetFragment as? CalendarFragment)?.apply {
            updateCalendar(type)
        }

        dismiss()
    }

}
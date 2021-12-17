package com.onirun.recyclerview

import android.content.Context
import com.onirun.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Aurelien Lubecki
 * on 24/03/2018.
 */
data class RaceSectionHeader(val date: Date, val nbRaces: Int) {

    companion object {

        private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.FRANCE)
    }

    fun getDisplayableDate(): String {
        return dateFormat.format(date).capitalize()
    }

    fun getDisplayableNbRaces(context: Context): String {
        return context.resources.getQuantityString(R.plurals.list_nb_events, nbRaces, nbRaces)
    }

}
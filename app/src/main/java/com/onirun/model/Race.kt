package com.onirun.model

import com.onirun.model.bundle.BundleRace
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
class Race(bundle: BundleRace)
    : BaseModel<Int>(bundle.raceId!!) {

    companion object {

        private val dateFormatHour = SimpleDateFormat("HH'h'mm", Locale.FRANCE)
        private val dateFormatDay = SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)
    }

    val raceId = id
    val eventId = bundle.eventId!!
    val name = bundle.name
    val description = bundle.description
    val type = Configuration.getInstance().findRaceType(bundle.type)!!

    val formats = bundle.formats?.asSequence()?.mapNotNull {
        Configuration.getInstance().findRaceFormat(it)
    }?.toSet() ?: setOf()

    val price = bundle.price
    val address = bundle.address
    val startTime = bundle.startTime?.let { Date(it * 1000) }
    val length = bundle.length

    fun getDisplayableLengthKm(): String? {

        if (length == null) {
            return null
        }

        if (length < 0) {
            return null
        }

        if (length < 1000) {
            //length in meters
            return "$length m"
        }

        if (length % 1000 == 0) {
            //length without comma
            return (length / 1000f).toInt().toString() + " km"
        }

        return NumberFormat.getNumberInstance(Locale.FRANCE).apply {
            maximumFractionDigits = 2
        }.format(length / 1000f) + " km"
    }

    fun getDisplayableStartTime(isEventDuringOneDay: Boolean): String? {

        if (startTime == null) {
            return null
        }

        if (isEventDuringOneDay) {
            //show only the hour
            return dateFormatHour.format(startTime)
        }

        //show the day and the hour
        return dateFormatDay.format(startTime).capitalize() + " à " + dateFormatHour.format(startTime)
    }

}
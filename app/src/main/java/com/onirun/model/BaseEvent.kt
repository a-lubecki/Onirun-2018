package com.onirun.model

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
abstract class BaseEvent(val eventId: Int, val name: String, val status: Int,
                         department: String, val city: String,
                         dateBegin: Long, dateEnd: Long)
    : BaseModel<Int>(eventId) {

    companion object {

        private val dateFormatDay = SimpleDateFormat("d", Locale.FRANCE)
        private val dateFormatMonth = SimpleDateFormat("MMMM", Locale.FRANCE)
        private val dateFormatYear = SimpleDateFormat("yyyy", Locale.FRANCE)
        private val dateFormatMonthYear = SimpleDateFormat("MMMM yyyy", Locale.FRANCE)
        private val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)
        private val longDateFormat = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRANCE)
    }

    val department = Configuration.getInstance().findDepartment(department)!!
    val dateBegin = Date(dateBegin * 1000)
    val dateEnd = Date(dateEnd * 1000)

    fun isDateValidated(): Boolean {
        return status != 10
    }

    val isDuringOneDay by lazy {

        val cBegin = Calendar.getInstance().apply {
            time = this@BaseEvent.dateBegin
        }
        val cEnd = Calendar.getInstance().apply {
            time = this@BaseEvent.dateEnd
        }

        cBegin.get(Calendar.DAY_OF_YEAR) == cEnd.get(Calendar.DAY_OF_YEAR)
    }

    val isBetweenMonths by lazy {

        val cBegin = Calendar.getInstance().apply {
            time = this@BaseEvent.dateBegin
        }
        val cEnd = Calendar.getInstance().apply {
            time = this@BaseEvent.dateEnd
        }

        cBegin.get(Calendar.MONTH) != cEnd.get(Calendar.MONTH)
    }

    fun getDisplayableDate(): String {
        return getDisplayableDate(dateFormat, dateFormatMonth)
    }

    fun getDisplayableLongDate(): String {
        return getDisplayableDate(longDateFormat, dateFormatMonthYear)
    }

    private fun getDisplayableDate(dateFormat1Day: DateFormat, dateFormat2DaysEnd: DateFormat): String {

        if (!isDateValidated()) {
            //the date is not approved, show only the month and year
            return "- - " + dateFormat2DaysEnd.format(dateBegin)
        }

        if (isDuringOneDay) {
            return dateFormat1Day.format(dateBegin).capitalize()
        }

        if (isBetweenMonths) {
            return "Du " + dateFormatDay.format(dateBegin) + " " + dateFormatMonth.format(dateBegin) + " au " +
                    dateFormatDay.format(dateEnd) + " " + dateFormat2DaysEnd.format(dateEnd)
        }

        return "Du " + dateFormatDay.format(dateBegin) + " au " +
                dateFormatDay.format(dateEnd) + " " + dateFormat2DaysEnd.format(dateEnd)
    }

    fun getDisplayableLocationDate(): String {
        return getDisplayableDate() + " • " + city + " (" + department.code + ")"
    }

    fun getDisplayableYear(): String {
        return dateFormatYear.format(dateBegin)
    }

}
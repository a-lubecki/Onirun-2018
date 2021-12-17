package com.onirun.model

import com.onirun.model.bundle.BundleRunnerEvent
import java.util.*

/**
 * Created by Aurelien Lubecki
 * on 20/03/2018.
 */
class RunnerEvent(bundle: BundleRunnerEvent)
    : BaseEvent(bundle.eventId!!, bundle.name, bundle.status,
        bundle.department, bundle.city,
        bundle.dateBegin!!, bundle.dateEnd!!) {

    val raceTypes = bundle.raceTypes?.asSequence()?.mapNotNull {
        Configuration.getInstance().findRaceType(it)
    }?.toSet() ?: setOf()

    val raceFormats = bundle.raceFormats?.asSequence()?.mapNotNull {
        Configuration.getInstance().findRaceFormat(it)
    }?.toSet() ?: setOf()

    val challenge = bundle.challenge
    val nbFriends = bundle.nbFriends
    val engagement = RaceEngagement.getEngagement(bundle.engagement)

    val monthOfDateBegin by lazy {

        val gc = GregorianCalendar(Locale.FRANCE)
        gc.time = dateBegin

        gc.set(Calendar.DAY_OF_MONTH, 1)
        gc.set(Calendar.HOUR_OF_DAY, 0)
        gc.set(Calendar.MINUTE, 0)
        gc.set(Calendar.SECOND, 0)
        gc.set(Calendar.MILLISECOND, 0)

        //ex : december 2018
        return@lazy Date(gc.timeInMillis)
    }

    fun getDisplayableTypes(): String {

        val s = StringBuilder()

        raceTypes.forEachIndexed { index, type ->

            s.append(type.shortName)

            if (index < raceTypes.size - 1) {
                s.append(" - ")
            }
        }

        return s.toString()
    }

}
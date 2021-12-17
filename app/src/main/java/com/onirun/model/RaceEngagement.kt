package com.onirun.model

/**
 * Created by Aurelien Lubecki
 * on 24/03/2018.
 */
enum class RaceEngagement(val trackingTag: String) {

    NOT_ENGAGED("delete"),
    PARTIALLY_ENGAGED("medium"),
    FULLY_ENGAGED("high");

    companion object {

        fun isEngaged(e: RaceEngagement): Boolean {
            return e == PARTIALLY_ENGAGED || e == FULLY_ENGAGED
        }

        fun getEngagement(value: Int): RaceEngagement {

            val e = RaceEngagement.values().find {
                it.ordinal == value
            }

            return when {
                e != null -> e
                value < 0 -> NOT_ENGAGED
                else -> FULLY_ENGAGED
            }
        }

    }

}
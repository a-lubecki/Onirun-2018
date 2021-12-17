package com.onirun.model

/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
enum class RaceCategory(val jsonValue: String) {

    CADET("CA"),
    SENIOR("SE"),
    VETERAN("VE");

    companion object {

        fun findRaceCategory(jsonValue: String?): RaceCategory? {

            if (jsonValue == null) {
                return null
            }

            return RaceCategory.values().find {
                it.jsonValue == jsonValue
            }
        }
    }

}
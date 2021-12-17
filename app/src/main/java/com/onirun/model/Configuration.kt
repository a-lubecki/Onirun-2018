package com.onirun.model

import com.mcxiaoke.koi.log.logw
import com.onirun.model.bundle.BundleConfiguration

/**
 * Created by Aurelien Lubecki
 * on 12/04/2018.
 */
class Configuration(bundle: BundleConfiguration) {


    companion object {

        private var config: Configuration? = null

        fun getInstance(): Configuration {
            return config!!
        }

        fun initInstance(c: Configuration) {

            if (config != null) {
                logw("model", "The config was already initialized")
                return
            }

            config = c
        }

        fun hasInstance(): Boolean {
            return config != null
        }
    }


    val departments = bundle.getDepartments().map {
        it.code to Department(it)
    }.toMap()

    val raceTypes = bundle.getRaceTypes().map {
        it.tag to RaceType(it)
    }.toMap()

    val raceFormats = bundle.getRaceFormats().map {
        it.tag to RaceFormat(it)
    }.toMap()

    val grades = bundle.getGrades().map {
        it.tag to RunnerGrade(it)
    }.toMap()


    fun hasDepartment(code: String): Boolean {
        return departments.containsKey(code)
    }

    fun findDepartment(code: String): Department? {
        return departments[code]
    }

    fun hasRaceType(tag: String): Boolean {
        return raceTypes.containsKey(tag)
    }

    fun findRaceType(tag: String): RaceType? {
        return raceTypes[tag]
    }

    fun hasRaceFormat(tag: String): Boolean {
        return raceFormats.containsKey(tag)
    }

    fun findRaceFormat(tag: String): RaceFormat? {
        return raceFormats[tag]
    }

    fun hasGrade(tag: String): Boolean {
        return grades.containsKey(tag)
    }

    fun findGrade(tag: String): RunnerGrade? {
        return grades[tag]
    }

}
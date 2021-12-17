package com.onirun.model.bundle

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 10/04/2018.
 */
data class BundleConfiguration(private val departments: List<BundleDepartment>,
                               private val raceTypes: List<BundleRaceType>,
                               private val raceFormats: List<BundleRaceFormat>,
                               private val grades: List<BundleRunnerGrade>) : IParsableBundle {

    fun getDepartments(): List<BundleDepartment> {

        return departments.filter {
            it.isValid()
        }
    }

    fun getRaceTypes(): List<BundleRaceType> {

        return raceTypes.filter {
            it.isValid()
        }
    }

    fun getRaceFormats(): List<BundleRaceFormat> {

        return raceFormats.filter {
            it.isValid()
        }
    }

    fun getGrades(): List<BundleRunnerGrade> {

        return grades.filter {
            it.isValid()
        }
    }

    override fun isValid(): Boolean {
        return true
    }

}
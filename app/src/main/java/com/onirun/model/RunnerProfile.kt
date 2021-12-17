package com.onirun.model

import com.onirun.model.bundle.BundleRunnerProfile

/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
class RunnerProfile(bundle: BundleRunnerProfile) {

    val grade = Configuration.getInstance().findGrade(bundle.grade ?: "")
    val gender = bundle.gender
    val nbYears = bundle.nbYears
    val nbTrainings = bundle.nbTrainings
    val ranSomeRaces = bundle.ranSomeRaces
    val nbRacesThisYear = bundle.nbRacesThisYear
    val raceCategory = RaceCategory.findRaceCategory(bundle.raceCategory)
    val isInClub = bundle.isInClub
    val clubName = bundle.clubName ?: ""

}
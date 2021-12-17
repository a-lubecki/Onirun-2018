package com.onirun.screens.account

import com.onirun.R
import com.onirun.model.RaceCategory
import com.onirun.model.RunnerProfile
import com.onirun.model.bundle.BundleRunnerProfile

enum class ProfileQuestion(val questionResId: Int, val answersResId: Array<Int>, val answersValue: Array<Any>,
                           val extractValueFromProfile: (profile: RunnerProfile) -> Any?,
                           val insertValueToBundle: (bundle: BundleRunnerProfile.Builder, value: Any) -> Unit,
                           val isEmoji: Boolean = false) {

    GENDER(
            R.string.profile_question_gender,
            arrayOf(R.string.profile_answer_gender0, R.string.profile_answer_gender1),
            arrayOf("female", "male"),
            {
                it.gender
            },
            { bundle, value ->
                bundle.setGender(value as String)
            },
            true
    ),

    NB_YEARS(
            R.string.profile_question_nb_years,
            arrayOf(R.string.profile_answer_nb_years0, R.string.profile_answer_nb_years1, R.string.profile_answer_nb_years2, R.string.profile_answer_nb_years3),
            arrayOf(0, 1, 3, 5),
            {
                it.nbYears
            },
            { bundle, value ->
                bundle.setNbYears(value as Int)
            }
    ),

    NB_TRAININGS(
            R.string.profile_question_nb_trainings,
            arrayOf(R.string.profile_answer_nb_trainings0, R.string.profile_answer_nb_trainings1, R.string.profile_answer_nb_trainings2),
            arrayOf(1, 3, 5),
            {
                it.nbTrainings
            },
            { bundle, value ->
                bundle.setNbTrainings(value as Int)
            }
    ),

    RAN_SOME_RACES(
            R.string.profile_question_ran_some_races,
            arrayOf(R.string.profile_answer_ran_some_races0, R.string.profile_answer_ran_some_races1),
            arrayOf(true, false),
            {
                it.ranSomeRaces
            },
            { bundle, value ->
                bundle.setRanSomeRaces(value as Boolean)
            }
    ),

    NB_RACES_THIS_YEAR(
            R.string.profile_question_nb_races_this_year,
            arrayOf(R.string.profile_answer_nb_race_this_year0, R.string.profile_answer_nb_race_this_year1, R.string.profile_answer_nb_race_this_year2, R.string.profile_answer_nb_race_this_year3),
            arrayOf(0, 1, 3, 5),
            {
                it.nbRacesThisYear
            },
            { bundle, value ->
                bundle.setNbRacesThisYear(value as Int)
            }
    ),

    RACE_CATEGORY(
            R.string.profile_question_race_category,
            arrayOf(R.string.profile_answer_race_category0, R.string.profile_answer_race_category1, R.string.profile_answer_race_category2),
            arrayOf(RaceCategory.CADET, RaceCategory.SENIOR, RaceCategory.VETERAN),
            {
                it.raceCategory
            },
            { bundle, value ->
                bundle.setRaceCategory((value as RaceCategory).jsonValue)
            }
    ),

    IS_IN_CLUB(
            R.string.profile_question_is_in_club,
            arrayOf(R.string.profile_answer_is_in_club0, R.string.profile_answer_is_in_club1),
            arrayOf(true, false),
            {
                it.isInClub
            },
            { bundle, value ->
                bundle.setIsInClub(value as Boolean)
            }
    );

    init {

        if (answersResId.size != answersValue.size) {
            throw IllegalStateException()
        }
    }

}
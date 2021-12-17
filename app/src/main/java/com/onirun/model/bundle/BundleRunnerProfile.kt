package com.onirun.model.bundle

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 12/04/2018.
 */
data class BundleRunnerProfile(val grade: String? = null, val gender: String? = null, val nbYears: Int? = null,
                               val nbTrainings: Int? = null, val ranSomeRaces: Boolean? = null,
                               val nbRacesThisYear: Int? = null, val raceCategory: String? = null,
                               val isInClub: Boolean? = null, val clubName: String? = null)
    : IParsableBundle {

    override fun isValid(): Boolean {
        return true
    }


    class Builder {

        var gender: String? = null
            private set
        var nbYears: Int? = null
            private set
        var nbTrainings: Int? = null
            private set
        var ranSomeRaces: Boolean? = null
            private set
        var nbRacesThisYear: Int? = null
            private set
        var raceCategory: String? = null
            private set
        var isInClub: Boolean? = null
            private set
        var clubName: String? = null
            private set

        fun setGender(gender: String?): Builder {
            this.gender = gender
            return this
        }

        fun setNbYears(nbYears: Int?): Builder {
            this.nbYears = nbYears
            return this
        }

        fun setNbTrainings(nbTrainings: Int?): Builder {
            this.nbTrainings = nbTrainings
            return this
        }

        fun setRanSomeRaces(ranSomeRaces: Boolean?): Builder {
            this.ranSomeRaces = ranSomeRaces
            return this
        }

        fun setNbRacesThisYear(nbRacesThisYear: Int?): Builder {
            this.nbRacesThisYear = nbRacesThisYear
            return this
        }

        fun setRaceCategory(raceCategory: String?): Builder {
            this.raceCategory = raceCategory
            return this
        }

        fun setIsInClub(isInClub: Boolean?): Builder {
            this.isInClub = isInClub
            return this
        }

        fun setClubName(clubName: String?): Builder {
            this.clubName = clubName
            return this
        }

        fun build(): BundleRunnerProfile {
            return BundleRunnerProfile(
                    null,
                    gender,
                    nbYears,
                    nbTrainings,
                    ranSomeRaces,
                    nbRacesThisYear,
                    raceCategory,
                    isInClub,
                    clubName
            )
        }

    }

}
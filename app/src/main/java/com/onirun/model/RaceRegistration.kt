package com.onirun.model

import com.onirun.model.bundle.BundleRaceRegistration

/**
 * Created by Aurelien Lubecki
 * on 17/04/2018.
 */

class RaceRegistration(bundle: BundleRaceRegistration)
    : BaseModel<Int>(bundle.raceId!!) {

    val raceId = id
    val challenge = bundle.challenge
    val engagement = RaceEngagement.getEngagement(bundle.engagement)
    val nbFriends = bundle.nbFriends

}

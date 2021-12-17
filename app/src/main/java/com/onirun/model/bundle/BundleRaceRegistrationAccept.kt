package com.onirun.model.bundle

import com.onirun.model.RaceEngagement

class BundleRaceRegistrationAccept(engagement: RaceEngagement?, val friendId: Int? = null) {

    val engagement = engagement?.ordinal ?: 0

}
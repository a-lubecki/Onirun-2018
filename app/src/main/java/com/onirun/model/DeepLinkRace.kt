package com.onirun.model

import com.onirun.model.bundle.BundleDeepLinkRace

class DeepLinkRace(bundle: BundleDeepLinkRace) {

    val raceId = bundle.raceId
    val name = bundle.name
    val type = Configuration.getInstance().findRaceType(bundle.type)

}
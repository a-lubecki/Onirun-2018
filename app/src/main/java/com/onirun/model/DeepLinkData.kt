package com.onirun.model

import com.onirun.model.bundle.BundleDeepLinkData

class DeepLinkData(bundle: BundleDeepLinkData) {

    val isFriend = bundle.isFriend
    val engagement = RaceEngagement.getEngagement(bundle.engagement)

    val runner = DeepLinkRunner(bundle.runner!!)
    val event = bundle.getEvent()?.let { DeepLinkEvent(it) }
    val race = bundle.getRace()?.let { DeepLinkRace(it) }

}
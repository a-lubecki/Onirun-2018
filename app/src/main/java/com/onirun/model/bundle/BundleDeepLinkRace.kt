package com.onirun.model.bundle

import com.onirun.model.Configuration

class BundleDeepLinkRace(val raceId: Int? = null, val name: String = "",
                         val type: String = "")
    : IParsableBundle {

    override fun isValid(): Boolean {
        return raceId != null &&
                raceId > 0 &&
                name.isNotBlank() &&
                Configuration.getInstance().hasRaceType(type)
    }

}
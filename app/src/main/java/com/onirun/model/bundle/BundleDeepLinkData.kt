package com.onirun.model.bundle

class BundleDeepLinkData(val isFriend: Boolean = false, val runner: BundleDeepLinkRunner? = null,
                         val engagement: Int, private val event: BundleDeepLinkEvent? = null,
                         private val race: BundleDeepLinkRace? = null)
    : IParsableBundle {

    fun getEvent(): BundleDeepLinkEvent? {

        if (event == null) {
            return null
        }

        if (!event.isValid()) {
            return null
        }

        return event
    }

    fun getRace(): BundleDeepLinkRace? {

        if (race == null) {
            return null
        }

        if (!race.isValid()) {
            return null
        }

        return race
    }

    override fun isValid(): Boolean {
        return runner != null &&
                runner.isValid()
    }

}
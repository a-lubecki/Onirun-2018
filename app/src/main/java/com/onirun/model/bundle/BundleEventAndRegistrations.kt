package com.onirun.model.bundle

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 14/04/2018.
 */
data class BundleEventAndRegistrations(val event: BundleEvent? = null,
                                       private val registrations: List<BundleRaceRegistration>? = null)
    : IParsableBundle {

    fun getRegistrations(): List<BundleRaceRegistration> {

        return registrations?.filter {
            it.isValid()
        } ?: listOf()
    }

    override fun isValid(): Boolean {
        return event != null &&
                event.isValid()
    }

}
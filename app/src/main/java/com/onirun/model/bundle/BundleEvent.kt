package com.onirun.model.bundle

import com.onirun.model.Configuration

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
data class BundleEvent(val eventId: Int? = null, val name: String = "",
                       val status: Int = 0, val description: String = "",
                       val illustration: String = "", val location: BundleLatLng? = null,
                       val department: String = "", val address: String = "",
                       val zipCode: String = "", val city: String = "",
                       val dateBegin: Long? = null, val dateEnd: Long? = null, private val races: List<BundleRace>? = null,
                       val urlPayment: String = "", val mail: String = "", val phone: String = "", val urlWebsite: String = "",
                       val urlFacebook: String = "", val urlTwitter: String = "", val urlInstagram: String = "")
    : IParsableBundle {

    fun getRaces(): List<BundleRace> {

        return races?.filter {
            it.isValid()
        } ?: listOf()
    }

    override fun isValid(): Boolean {

        return eventId != null &&
                eventId > 0 &&
                name.isNotBlank() &&
                Configuration.getInstance().hasDepartment(department) &&
                city.isNotEmpty() &&
                dateBegin != null &&
                dateEnd != null
    }

}
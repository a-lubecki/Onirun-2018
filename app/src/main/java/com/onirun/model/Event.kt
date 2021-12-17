package com.onirun.model

import com.onirun.model.bundle.BundleEvent

/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
class Event(bundle: BundleEvent)
    : BaseEvent(bundle.eventId!!, bundle.name, bundle.status,
        bundle.department, bundle.city,
        bundle.dateBegin!!, bundle.dateEnd!!) {

    val description = bundle.description
    val illustration = bundle.illustration
    val location = bundle.location?.newLatLng()
    val address = bundle.description
    val zipCode = bundle.zipCode
    val urlPayment = bundle.urlPayment
    val urlWebsite = bundle.urlWebsite
    val urlFacebook = bundle.urlFacebook
    val urlTwitter = bundle.urlTwitter
    val urlInstagram = bundle.urlInstagram
    val mail = bundle.mail
    val phone = bundle.phone

    val races = bundle.getRaces().map {
        Race(it)
    }

    fun hasIllustration(): Boolean {
        return illustration.isNotEmpty()
    }

}
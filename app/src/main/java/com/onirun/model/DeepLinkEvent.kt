package com.onirun.model

import com.onirun.model.bundle.BundleDeepLinkEvent
import java.util.*

class DeepLinkEvent(bundle: BundleDeepLinkEvent) {

    val eventId = bundle.eventId
    val name = bundle.name
    val challenge = bundle.challenge
    val dateBegin = bundle.dateBegin?.let { Date(it * 1000) }
    val city = bundle.city

}
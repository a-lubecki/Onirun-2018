package com.onirun.model.bundle

class BundleDeepLinkEvent(val eventId: Int? = null, val name: String = "",
                          val challenge: Boolean = false, val dateBegin: Long? = null,
                          val city: String = "")
    : IParsableBundle {

    override fun isValid(): Boolean {

        return eventId != null &&
                eventId > 0 &&
                name.isNotBlank() &&
                dateBegin != null
    }

}
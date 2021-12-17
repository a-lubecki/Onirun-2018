package com.onirun.model

import com.onirun.model.bundle.BundleNotification
import java.util.*


class Notification(bundle: BundleNotification)
    : BaseModel<Int>(bundle.notifId!!) {

    val notifId = id
    val seen = bundle.seen
    val type = NotificationType.findNotificationType(bundle.type)!!
    val emoji = bundle.emoji
    val message = bundle.message
    val dateAdd = Date(bundle.dateAdd!! * 1000)
    val data = bundle.data ?: mapOf()

}

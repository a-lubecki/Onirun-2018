package com.onirun.recyclerview

import android.view.View
import com.onirun.model.NotificationType

data class NotificationSettingsSection(val title: String, val subtitle: String, val type: NotificationType,
                                       val hasPush: Boolean, val hasMail: Boolean) {


    val layoutPushVisibility = if (hasPush) View.VISIBLE else View.GONE
    val layoutMailVisibility = if (hasMail) View.VISIBLE else View.GONE
    val separatorVisibility = if (hasPush && hasMail) View.VISIBLE else View.GONE

}

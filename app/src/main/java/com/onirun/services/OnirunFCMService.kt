package com.onirun.services

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.onirun.api.UserManager
import com.onirun.screens.notification.NotificationClickManager


class OnirunFCMService : FirebaseMessagingService() {


    companion object {

        const val INTENT_FILTER_NOTIF_RECEIVED = "onirun_notif_received"
    }

    override fun onNewToken(token: String?) {

        //send the new token to the server
        UserManager.registerFCMToken(this, token)
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)

        if (message?.data?.get(NotificationClickManager.KEY_NOTIFICATION_ID) == null) {
            //bad formatted notif
            return
        }

        //add a badge on the main bottom nav bar in MainActivity (received with a BroadcastReceiver)
        sendBroadcast(Intent(INTENT_FILTER_NOTIF_RECEIVED))
    }

}
package com.onirun

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.mcxiaoke.koi.KoiConfig
import io.fabric.sdk.android.Fabric


/**
 * Created by Aurelien Lubecki
 * on 19/03/2018.
 */

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        if (BuildConfig.IS_CRASHLYTICS_ENABLED) {
            Fabric.with(this, Crashlytics())
        }

        if (BuildConfig.IS_LOGGER_ENABLED) {
            KoiConfig.logEnabled = true
        }

        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

}

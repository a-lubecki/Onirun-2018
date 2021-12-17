package com.onirun.api

import android.content.Context
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Created by Aurelien Lubecki
 * on 15/04/2018.
 */
object HttpClientManager {

    fun newHttpClientBuilder(context: Context): OkHttpClient.Builder {
        return OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
    }

}
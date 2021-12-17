package com.onirun.api

import android.content.Context
import com.onirun.test.JSONDifferenceLogger
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


/**
 * Created by Aurelien Lubecki
 * on 15/04/2018.
 */
object HttpClientManager {

    fun newHttpClientBuilder(context: Context): OkHttpClient.Builder {

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        //create the http client with fake responses in debug
        val b = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logging)

        if (APIManager.isAPITestModeEnabled) {
            b.addInterceptor(FakeResponseInterceptor(context))
        }

        if (APIManager.isAPILogAlertEnabled) {

            b.addInterceptor { chain ->

                val response = chain.proceed(chain.request())

                val body = response.body() ?: return@addInterceptor response

                body.source().request(java.lang.Long.MAX_VALUE)
                val bodyCopy = ResponseBody.create(body.contentType(), -1, body.source().buffer().clone())

                JSONDifferenceLogger.registerAPIResponse(chain.request().url(), bodyCopy.string())

                response
            }
        }

        return b
    }

}
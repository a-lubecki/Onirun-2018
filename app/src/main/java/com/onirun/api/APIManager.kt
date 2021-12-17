package com.onirun.api

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.mcxiaoke.koi.ext.longToast
import com.mcxiaoke.koi.ext.toast
import com.mcxiaoke.koi.log.logw
import com.onirun.BuildConfig
import com.onirun.R
import com.onirun.model.bundle.IParsableBundle
import com.onirun.screens.main.BaseActivity
import com.onirun.test.JSONDifferenceLogger
import com.onirun.utils.alertDialog
import com.onirun.utils.showIfActivityRunning
import okhttp3.Cache
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


/**
 * Created by Aurelien Lubecki
 * on 21/03/2018.
 */
object APIManager {


    private const val HEADER_AUTH_TOKEN = "X-Authorization-Firebase"


    var isAPITestModeEnabled = false
    var isAPILogAlertEnabled = false


    fun newAuthorizedImageUrl(imageId: String): GlideUrl? {

        if (imageId.isEmpty()) {
            return null
        }

        return GlideUrl(BuildConfig.ONIRUN_URL_API_STORAGES + imageId, LazyHeaders.Builder()
                .addHeader(APIManager.HEADER_AUTH_TOKEN, UserManager.authToken!!)
                .build())
    }

    fun getAPICacheFile(context: Context): File {
        return File(context.cacheDir, "retrofitCache")
    }

    fun <T> call(context: Context, getTask: ((APIService) -> Call<T>),
                 needsAuth: Boolean, hasCache: Boolean, onSuccess: ((T) -> Unit)?, onError: ((ErrorResponse?) -> Unit)?) {

        val httpClientBuilder = HttpClientManager.newHttpClientBuilder(context)

        //add response caching, oldest entries are cleared if the max size is reached
        if (hasCache) {
            httpClientBuilder.cache(Cache(getAPICacheFile(context), 10 * 1024 * 1024))//10MB cache size
        }

        //add auth token from firebase if needed
        if (needsAuth) {

            httpClientBuilder.addInterceptor { chain ->

                if (!UserManager.isAuthTokenValid()) {
                    return@addInterceptor chain.proceed(chain.request())
                }

                chain.proceed(
                        chain.request().newBuilder()
                                .addHeader(HEADER_AUTH_TOKEN, UserManager.authToken!!)
                                .build()
                )
            }
        }

        val retrofit = Retrofit.Builder()
                .client(httpClientBuilder.build())
                .baseUrl(BuildConfig.ONIRUN_URL_API)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(APIService::class.java)

        if (needsAuth && !UserManager.isAuthTokenValid()) {

            //token not valid, get it from firebase before calling the server
            UserManager.retrieveAuthTokenOrLogout(context, {

                processCall(context, service, getTask, onSuccess, onError)

            }, {

                invokeErrorIfActivityValid(context, onError)
            })

            return
        }

        processCall(context, service, getTask, onSuccess, onError)
    }

    private fun <T> processCall(context: Context, service: APIService, getTask: ((APIService) -> Call<T>), onSuccess: ((T) -> Unit)?, onError: ((ErrorResponse?) -> Unit)?) {

        getTask(service).enqueue(object : Callback<T> {

            override fun onResponse(call: Call<T>?, response: Response<T>?) {

                if (call == null || response == null) {
                    showAPIError(context, "Problem : null call or response", null, true)
                    invokeErrorIfActivityValid(context, onError)
                    return
                }

                val httpCode = response.code()

                when (httpCode) {
                    in 200 until 300 -> handleSuccess(context, onSuccess, onError, call, response)
                    in 400 until 500 -> handleClientError(context, onError, response)
                    in 500 until 600 -> handleServerError(context, onError, response)
                    else -> invokeErrorIfActivityValid(context, onError)
                }
            }

            override fun onFailure(call: Call<T>?, t: Throwable?) {

                showAPIError(context, "API call failed", t, true)

                invokeErrorIfActivityValid(context, onError)
            }
        })

    }

    private fun <T> handleSuccess(context: Context, onSuccess: ((T) -> Unit)?, onError: ((ErrorResponse?) -> Unit)?, call: Call<T>, response: Response<T>) {

        var data = response.body()

        if (data == null) {
            showAPIError(context, "Problem : data retrieved but not parsed", null, false)
            return
        }

        if (isAPILogAlertEnabled) {

            val url = call.request().url()

            //show a dialog with the API response
            try {
                val logs = JSONDifferenceLogger.getDifferencesLogs(url, data)

                AlertDialog.Builder(context)
                        .setTitle("DEBUG")
                        .setMessage(logs)
                        .setNegativeButton("Fermer", null)
                        .setNeutralButton("Copier le JSON") { _, _ ->

                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("text", logs + "\n" + JSONDifferenceLogger.getJson(url))
                            clipboard.primaryClip = clip

                            context.longToast("Copié dans le presse-papier :\n$url")
                        }
                        .setOnDismissListener {

                            JSONDifferenceLogger.freeAPIResponse(url)
                        }
                        .create()
                        .show()

            } catch (e: Exception) {
                logw("DEBUG ERROR", e)
                JSONDifferenceLogger.freeAPIResponse(url)
            }
        }

        when (data) {
            is IParsableBundle -> {

                if (!data.isValid()) {
                    showAPIError(context, "Problem : data retrieved but not valid", null, true)
                    invokeErrorIfActivityValid(context, onError)
                    return
                }
            }
            is Collection<*> -> {

                //remove non-valid elements from list
                @Suppress("UNCHECKED_CAST")
                data = data.filter {
                    (it as? IParsableBundle)?.isValid() ?: true
                } as T
            }
        }

        invokeSuccessIfActivityValid(context, onSuccess, data)
    }

    private fun <T> handleClientError(context: Context, onError: ((ErrorResponse?) -> Unit)?, response: Response<T>) {

        val httpCode = response.code()

        if (httpCode == 401) {
            UserManager.logout(context)
            return
        }

        var message: String? = null
        var errorResponse: ErrorResponse? = null

        if (httpCode == 404) {

            //cant retry
            message = context.getString(R.string.error_message_not_found)

        } else {

            try {
                val errorJson = JSONObject(response.errorBody()?.string() ?: "")

                message = errorJson.optString("message")
                val errorData = errorJson.optJSONObject("data")

                errorResponse = ErrorResponse(httpCode, errorData)

            } catch (e: Exception) {
            }
        }

        if (errorResponse == null) {
            //default error response with null data
            errorResponse = ErrorResponse(response.code(), null)
        }

        if (!message.isNullOrEmpty()) {

            //display 400 error
            context.alertDialog()
                    .setTitle(R.string.error_title_default)
                    .setMessage(message)
                    .setNegativeButton(android.R.string.ok) { _, _ ->
                        invokeErrorIfActivityValid(context, onError, errorResponse)
                    }
                    .setOnCancelListener {
                        invokeErrorIfActivityValid(context, onError, errorResponse)
                    }
                    .showIfActivityRunning()

            return
        }

        showAPIError(context, "Problem : $httpCode", null, false)

        invokeErrorIfActivityValid(context, onError, errorResponse)
    }

    private fun <T> handleServerError(context: Context, onError: ((ErrorResponse?) -> Unit)?, response: Response<T>) {

        val errorResponse = ErrorResponse(response.code(), null)

        context.alertDialog()
                .setTitle(R.string.error_title_default)
                .setMessage(R.string.error_message_maintenance)
                .setNegativeButton(android.R.string.ok) { _, _ ->
                    invokeErrorIfActivityValid(context, onError, errorResponse)
                }
                .setOnCancelListener {
                    invokeErrorIfActivityValid(context, onError, errorResponse)
                }
                .showIfActivityRunning()
    }


    private fun <T> invokeSuccessIfActivityValid(context: Context, onSuccess: ((T) -> Unit)?, data: T?) {

        val activity = context as? BaseActivity
        if (activity != null && !activity.isActivityValid()) {
            return
        }

        onSuccess?.invoke(data!!)
    }

    private fun invokeErrorIfActivityValid(context: Context, onError: ((ErrorResponse?) -> Unit)?, errorResponse: ErrorResponse? = null) {

        if (onError == null) {
            return
        }

        val activity = context as? BaseActivity
        if (activity != null && !activity.isActivityValid()) {
            return
        }

        onError.invoke(errorResponse)
    }

    fun showAPIError(context: Context, message: String, e: Throwable?, showToUser: Boolean) {

        logw(message, e)

        if (showToUser) {
            context.toast(R.string.error_message_default)
        }
    }

}

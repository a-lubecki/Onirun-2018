package com.onirun.test

import com.mcxiaoke.koi.log.logd
import com.onirun.model.bundle.IParsableBundle
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject

object JSONDifferenceLogger {

    private const val SEPARATOR = "."

    private var jsonResponses = mutableMapOf<HttpUrl, String>()


    fun getJson(url: HttpUrl): String? {
        return jsonResponses[url]
    }

    fun registerAPIResponse(url: HttpUrl, json: String?) {

        if (json.isNullOrEmpty()) {
            return
        }

        jsonResponses[url] = json
    }

    fun freeAPIResponse(url: HttpUrl) {

        jsonResponses.remove(url)
    }

    fun getDifferencesLogs(url: HttpUrl, responseObject: Any): String? {

        if (!jsonResponses.containsKey(url)) {
            return null
        }

        val json = jsonResponses[url]!!

        val strLog = StringBuilder()
        strLog.append("JSON diff for : ", url.url(), "\n")

        when {
            json.startsWith("[") -> logArrayDifferences(strLog, FieldsSummary((responseObject as Collection<*>).javaClass.genericInterfaces[0].javaClass as Class<IParsableBundle>), JSONArray(json), "")
            json.startsWith("{") -> logObjectDifferences(strLog, FieldsSummary(responseObject as IParsableBundle), JSONObject(json), "")
            else -> {
                logd("JSONDifferenceLogger", "PROBLEM with JSON format")
                return null
            }
        }

        return strLog.toString()
    }

    private fun logArrayDifferences(strLog: StringBuilder, fieldsSummary: FieldsSummary, jsonArray: JSONArray?, path: String) {

        if (jsonArray == null) {
            return
        }

        for (i in 0 until jsonArray.length()) {

            logObjectDifferences(strLog, fieldsSummary, jsonArray.optJSONObject(i), path + i + SEPARATOR)
        }

    }

    private fun logObjectDifferences(strLog: StringBuilder, fieldsSummary: FieldsSummary, jsonObject: JSONObject?, path: String) {

        if (jsonObject == null) {
            return
        }

        //check if a field is missing from json
        fieldsSummary.fieldNames().forEach { name ->

            if (!jsonObject.has(name)) {

                strLog.append("\n >> manquant : ", path, name)

            } else if (jsonObject.isNull(name)) {

                strLog.append("\n >> null : ", path, name)

            } else {

                //test if empty array
                if (fieldsSummary.isArray(name)) {

                    val jsonArray = jsonObject.optJSONArray(name)
                    if (jsonArray.length() <= 0) {
                        strLog.append("\n >> array vide : ", path, name)
                    }
                }
            }

        }

        //check if json has too much fields
        jsonObject.keys().forEach { name ->

            if (!fieldsSummary.has(name)) {
                strLog.append("\n >> en trop : ", path, name)
            }
        }

        fieldsSummary.getObjectChildrenNames().forEach { name ->

            logObjectDifferences(
                    strLog,
                    fieldsSummary.getChildSummary(name)!!,
                    jsonObject.optJSONObject(name),
                    path + name + SEPARATOR
            )
        }

        fieldsSummary.getObjectArrayChildrenNames().forEach { name ->

            logArrayDifferences(
                    strLog,
                    fieldsSummary.getChildSummary(name)!!,
                    jsonObject.optJSONArray(name),
                    path + name + SEPARATOR
            )
        }

    }

}
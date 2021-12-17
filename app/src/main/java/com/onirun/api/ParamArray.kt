package com.onirun.api

import org.json.JSONArray

/**
 * Created by Aurelien Lubecki
 * on 21/03/2018.
 */
class ParamArray<T>(vararg val params: T) {

    override fun toString(): String {
        return JSONArray(params).toString()
    }

}
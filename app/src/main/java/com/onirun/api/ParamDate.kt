package com.onirun.api

import java.util.*

/**
 * Created by Aurelien Lubecki
 * on 21/03/2018.
 */
class ParamDate(val param: Date) {

    override fun toString(): String {
        return (param.time / 1000).toString()
    }

}
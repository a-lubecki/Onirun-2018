package com.onirun.model

/**
 * Created by Aurelien Lubecki
 * on 13/04/2018.
 */
abstract class BaseRunner(val runnerId: Int, userName: String?,
                          val grade: RunnerGrade?)
    : BaseModel<Int>(runnerId) {

    val userName = userName ?: ""

    fun getGradeName(): String {
        return grade?.name ?: ""
    }
}
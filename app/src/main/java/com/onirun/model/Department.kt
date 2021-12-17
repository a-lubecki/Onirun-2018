package com.onirun.model

import com.onirun.model.bundle.BundleDepartment

/**
 * Created by Raven
 * on 05/03/2018.
 */
class Department(bundle: BundleDepartment)
    : BaseModel<String>(bundle.code) {

    val code = id
    val name = bundle.name

    fun getDisplayableCode(): String {
        return "[$code]"
    }
}
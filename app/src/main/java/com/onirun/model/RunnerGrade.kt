package com.onirun.model

import com.onirun.model.bundle.BundleRunnerGrade

/**
 * Created by Aurelien Lubecki
 * on 24/03/2018.
 */
class RunnerGrade(bundle: BundleRunnerGrade)
    : BaseModel<String>(bundle.tag) {

    val tag = id
    val name = bundle.name

}
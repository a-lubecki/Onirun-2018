package com.onirun.model

import com.onirun.model.bundle.BundleRunner


/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
class Runner(bundle: BundleRunner)
    : BaseRunner(bundle.runnerId!!, bundle.userName,
        Configuration.getInstance().findGrade(bundle.grade)) {

    val settings = bundle.settings?.let {
        RunnerSettings(it)
    } ?: RunnerSettings(null)

    val nbFriends = bundle.nbFriends

}
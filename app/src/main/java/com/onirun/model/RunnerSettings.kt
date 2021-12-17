package com.onirun.model

import com.onirun.model.bundle.BundleRunnerSettings

/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
class RunnerSettings(bundle: BundleRunnerSettings?) {

    val departments = bundle?.departments?.asSequence()?.mapNotNull {
        Configuration.getInstance().findDepartment(it)
    }?.toSet() ?: setOf()

    val raceTypes = bundle?.raceTypes?.asSequence()?.mapNotNull {
        Configuration.getInstance().findRaceType(it)
    }?.toSet() ?: setOf()

    val raceFormats = bundle?.raceFormats?.asSequence()?.mapNotNull {
        Configuration.getInstance().findRaceFormat(it)
    }?.toSet() ?: setOf()

}
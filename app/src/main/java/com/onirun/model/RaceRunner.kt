package com.onirun.model

import com.onirun.model.bundle.BundleRaceRunner

/**
 * Created by Aurelien Lubecki
 * on 13/04/2018.
 */
class RaceRunner(bundle: BundleRaceRunner)
    : BaseRunner(bundle.runnerId!!, bundle.userName,
        Configuration.getInstance().findGrade(bundle.grade)) {

    val engagement = RaceEngagement.getEngagement(bundle.engagement)

}
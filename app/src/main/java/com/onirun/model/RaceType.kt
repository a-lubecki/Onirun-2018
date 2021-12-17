package com.onirun.model

import com.onirun.model.bundle.BundleRaceType

/**
 * Created by Aurelien Lubecki
 * on 24/03/2018.
 */
class RaceType(bundle: BundleRaceType)
    : BaseModel<String>(bundle.tag) {

    val tag = id
    val group = RaceTypeGroup.findGroup(bundle.group)!!
    val name = bundle.name
    val shortName = bundle.shortName

}
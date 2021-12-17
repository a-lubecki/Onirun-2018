package com.onirun.model

import com.onirun.model.bundle.BundleRaceFormat
import com.onirun.utils.toBitmap


/**
 * Created by Aurelien Lubecki
 * on 24/03/2018.
 */
class RaceFormat(bundle: BundleRaceFormat)
    : BaseModel<String>(bundle.tag) {

    val tag = id
    val name = bundle.name
    val emoji = bundle.emoji.toBitmap()

}
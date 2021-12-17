package com.onirun.model

import com.onirun.model.bundle.BundleFriendRunner

/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
class FriendRunner(bundle: BundleFriendRunner)
    : BaseRunner(bundle.runnerId!!, bundle.userName,
        Configuration.getInstance().findGrade(bundle.grade)) {

    val nbEvents = bundle.nbEvents

}
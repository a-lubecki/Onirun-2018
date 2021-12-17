package com.onirun.model.bundle


class BundleDeepLinkRunner(val runnerId: Int? = null, val userName: String? = null,
                           val grade: String = "")
    : IParsableBundle {

    override fun isValid(): Boolean {
        return runnerId != null &&
                runnerId > 0
    }

}
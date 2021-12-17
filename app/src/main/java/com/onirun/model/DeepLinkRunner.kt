package com.onirun.model

import com.onirun.model.bundle.BundleDeepLinkRunner

class DeepLinkRunner(bundle: BundleDeepLinkRunner)
    : BaseRunner(bundle.runnerId!!, bundle.userName, Configuration.getInstance().findGrade(bundle.grade))

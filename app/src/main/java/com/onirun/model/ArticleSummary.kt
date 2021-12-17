package com.onirun.model

import com.onirun.model.bundle.BundleArticleSummary

/**
 * Created by Aurelien Lubecki
 * on 20/03/2018.
 */
class ArticleSummary(bundle: BundleArticleSummary)
    : BaseArticle(bundle.slug, bundle.title, bundle.illustration) {

    val subtitle = bundle.subtitle

}
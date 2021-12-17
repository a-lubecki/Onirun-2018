package com.onirun.model

/**
 * Created by Aurelien Lubecki
 * on 17/03/2018.
 */
abstract class BaseArticle(val slug: String, val title: String, val illustration: String)
    : BaseModel<String>(slug)
package com.onirun.screens.news

import android.content.Context
import android.widget.FrameLayout
import com.onirun.model.BaseArticleContentPart

/**
 * Created by Aurelien Lubecki
 * on 16/04/2018.
 */
abstract class BaseRowArticleContentPart<P : BaseArticleContentPart>(context: Context) : FrameLayout(context) {

    private lateinit var part: P

    open fun setContentPart(contentPart: P) {
        part = contentPart
    }

}
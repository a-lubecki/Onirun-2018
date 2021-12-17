package com.onirun.screens.news

import android.annotation.SuppressLint
import android.content.Context
import com.bumptech.glide.Glide
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.model.ArticleContentImage
import kotlinx.android.synthetic.main.row_article_content_image.view.*

/**
 * Created by Aurelien Lubecki
 * on 16/04/2018.
 */
class RowArticleContentImage(context: Context) : BaseRowArticleContentPart<ArticleContentImage>(context) {


    init {
        inflate(context, R.layout.row_article_content_image, this)
    }

    @SuppressLint("SetTextI18n")
    override fun setContentPart(contentPart: ArticleContentImage) {
        super.setContentPart(contentPart)

        Glide.with(this)
                .load(APIManager.newAuthorizedImageUrl(contentPart.illustration))
                .into(imageViewIllustration)
    }

}
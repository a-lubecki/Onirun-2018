package com.onirun.screens.news

import android.annotation.SuppressLint
import android.content.Context
import com.onirun.R
import com.onirun.model.ArticleContentQuote
import kotlinx.android.synthetic.main.row_article_content_quote.view.*

/**
 * Created by Aurelien Lubecki
 * on 16/04/2018.
 */
class RowArticleContentQuote(context: Context) : BaseRowArticleContentPart<ArticleContentQuote>(context) {


    init {
        inflate(context, R.layout.row_article_content_quote, this)
    }

    @SuppressLint("SetTextI18n")
    override fun setContentPart(contentPart: ArticleContentQuote) {
        super.setContentPart(contentPart)

        textViewText.text = contentPart.text
        textViewAuthor.text = "- " + contentPart.author
    }

}
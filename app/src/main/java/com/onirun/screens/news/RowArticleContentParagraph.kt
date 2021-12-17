package com.onirun.screens.news

import android.content.Context
import com.onirun.R
import com.onirun.model.ArticleContentParagraph
import com.onirun.utils.setVisible
import kotlinx.android.synthetic.main.row_article_content_paragraph.view.*

/**
 * Created by Aurelien Lubecki
 * on 16/04/2018.
 */
class RowArticleContentParagraph(context: Context) : BaseRowArticleContentPart<ArticleContentParagraph>(context) {


    init {
        inflate(context, R.layout.row_article_content_paragraph, this)
    }

    override fun setContentPart(contentPart: ArticleContentParagraph) {
        super.setContentPart(contentPart)

        if (contentPart.title.isEmpty()) {
            textViewTitle.setVisible(false)
        } else {
            textViewTitle.setVisible(true)
            textViewTitle.text = contentPart.title
        }

        textViewText.text = contentPart.text
    }

}
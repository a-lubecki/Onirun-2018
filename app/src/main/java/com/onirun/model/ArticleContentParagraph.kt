package com.onirun.model

class ArticleContentParagraph(
        type: ArticleContentPartType,
        val title: String,
        val text: String)
    : BaseArticleContentPart(type)

package com.onirun.model

class ArticleContentQuote(
        type: ArticleContentPartType,
        val text: String,
        val author: String)
    : BaseArticleContentPart(type)

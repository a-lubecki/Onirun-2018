package com.onirun.model

class ArticleContentEvents(
        type: ArticleContentPartType,
        val events: List<RunnerEvent>)
    : BaseArticleContentPart(type)

package com.onirun.screens.news

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.widget.LinearLayout
import com.onirun.R
import com.onirun.databinding.RowRunnerEventBinding
import com.onirun.model.ArticleContentEvents
import com.onirun.recyclerview.RunnerEventListHandler
import com.onirun.screens.event.EventActivity

/**
 * Created by Aurelien Lubecki
 * on 16/04/2018.
 */
class RowArticleContentEvents(context: Context) : BaseRowArticleContentPart<ArticleContentEvents>(context) {


    init {
        inflate(context, R.layout.row_article_content_events, this)
    }

    @SuppressLint("SetTextI18n")
    override fun setContentPart(contentPart: ArticleContentEvents) {
        super.setContentPart(contentPart)

        val activity = context as Activity

        val layoutPart = getChildAt(0) as LinearLayout

        contentPart.events.forEach { event ->

            val binding = DataBindingUtil.inflate<RowRunnerEventBinding>(activity.layoutInflater, R.layout.row_runner_event, this, false)
            binding.item = event

            RunnerEventListHandler.completeRunnerEventBind(binding, event)

            binding.root.setOnClickListener {
                EventActivity.startForResultApiReload(activity, event.eventId)
            }

            layoutPart.addView(binding.root)
        }
    }

}
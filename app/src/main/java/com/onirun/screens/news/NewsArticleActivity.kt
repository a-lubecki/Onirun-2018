package com.onirun.screens.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.model.*
import com.onirun.model.bundle.BundleArticle
import com.onirun.screens.event.EventActivity
import com.onirun.screens.main.BaseLoadingActivity
import com.onirun.utils.setVisible
import com.onirun.utils.share
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_news_article.*

class NewsArticleActivity : BaseLoadingActivity() {


    companion object {

        private const val EXTRA_SLUG = "slug"

        fun start(context: Context, slug: String) {

            newIntent(context, slug)
                    .start(context)
        }

        fun newIntent(context: Context, slug: String): Intent {

            return context.newIntent<NewsArticleActivity>()
                    .putExtra(EXTRA_SLUG, slug)
        }

    }


    override val layoutId = R.layout.activity_news_article

    private lateinit var article: Article


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retrieveArticle()
    }

    override fun onAPIReloadRequested(data: Intent?) {
        super.onAPIReloadRequested(data)

        //one event has changed in the next screen : update the previous activity
        setResultAPIReload()

        retrieveArticle()
    }

    private fun retrieveArticle() {

        showLoaderDelayed()

        setContentVisible(false)

        scrollView.scrollTo(0, 0)

        APIManager.call(
                this,
                {
                    it.getNewsArticle(
                            intent.getStringExtra(EXTRA_SLUG)
                    )
                },
                true,
                false,
                {

                    hideLoader()

                    displayArticle(it)
                },
                {
                    finish()
                }
        )
    }

    private fun setContentVisible(visible: Boolean) {

        layoutAppBarContent.setVisible(visible)
        layoutContent.setVisible(visible)
    }

    private fun displayArticle(response: BundleArticle) {

        article = Article(response)

        setContentVisible(true)

        Glide.with(this)
                .load(APIManager.newAuthorizedImageUrl(article.illustration))
                .into(imageViewIllustration)

        textViewTitle.text = article.title

        textViewSubtitle.text = getString(R.string.article_subtitle_selection)

        textViewCity.setVisible(false)

        footerArticleEvent.setVisible(false)
        footerArticleVIP.setVisible(false)

        when (article.type) {

            ArticleType.EVENT -> {

                val event = article.specificEvent

                textViewCity.setVisible(true)

                var textLocation = event?.city ?: ""
                event?.department?.let {
                    textLocation += " (${it.code})"
                }
                textViewCity.text = textLocation

                //show footer if the event id is available
                event?.eventId?.let { eventId ->

                    footerArticleEvent.setVisible(true)
                    footerArticleEvent.setEvent(article.title) {
                        EventActivity.startForResultApiReload(this, eventId)
                    }
                }
            }

            ArticleType.VIP -> {

                textViewSubtitle.text = getString(R.string.article_subtitle_vip)

                footerArticleVIP.setVisible(true)
                footerArticleVIP.setVIP(article.specificVIP)
            }

            else -> {
                //do nothing
            }
        }


        loadContent(article.content)


        if (article.shareUrl.isEmpty()) {

            buttonShareArticle.setVisible(false)

        } else {

            buttonShareArticle.setVisible(true)

            buttonShareArticle.setOnClickListener {

                article.shareUrl.share { intent ->
                    startActivity(intent)
                }
            }
        }

    }

    private fun loadContent(content: List<BaseArticleContentPart>) {

        layoutContainerContent.removeAllViews()

        content.forEach { part ->

            when (part) {

                is ArticleContentParagraph -> addContentPart(RowArticleContentParagraph(this), part)

                is ArticleContentQuote -> addContentPart(RowArticleContentQuote(this), part)

                is ArticleContentImage -> addContentPart(RowArticleContentImage(this), part)

                is ArticleContentEvents -> addContentPart(RowArticleContentEvents(this), part)
            }
        }

    }

    private fun <P : BaseArticleContentPart> addContentPart(view: BaseRowArticleContentPart<P>, part: P) {

        view.setContentPart(part)

        layoutContainerContent.addView(view)
    }

}
package com.onirun.screens.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type
import com.onirun.BR
import com.onirun.BuildConfig
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.databinding.RowArticleSummaryBinding
import com.onirun.model.ArticleSummary
import com.onirun.recyclerview.LazyLoadingActivityHandler
import com.onirun.recyclerview.LazyLoadingFooter
import com.onirun.recyclerview.LazyLoadingListHandlerListener
import com.onirun.screens.main.BaseMenuFragment
import com.onirun.test.APIDebugButtons
import kotlinx.android.synthetic.main.recyclerview_default.*

class NewsFragment : BaseMenuFragment(), LazyLoadingListHandlerListener {


    override val baseLayoutContent: ViewGroup? by lazy {
        recyclerView
    }

    private lateinit var lazyLoadingHandler: LazyLoadingActivityHandler


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO TEST
        if (BuildConfig.CAN_DEBUG_API) {
            APIDebugButtons(view as ViewGroup).init()
        }

        val items = mutableListOf<Any>()
        val adapter = LastAdapter(items, BR.item)
                .into(recyclerView)
                .map<LazyLoadingFooter>(R.layout.row_loader)
                .map<ArticleSummary>(Type<RowArticleSummaryBinding>(R.layout.row_article_summary)
                        .onBind {

                            val articleSummary = it.binding.item ?: return@onBind

                            Glide.with(this)
                                    .load(APIManager.newAuthorizedImageUrl(articleSummary.illustration))
                                    .into(it.binding.imageViewIllustration)
                        }
                        .onClick {

                            val articleSummary = it.binding.item ?: return@onClick

                            NewsArticleActivity.start(recyclerView.context, articleSummary.slug)
                        })

        lazyLoadingHandler = LazyLoadingActivityHandler(
                items,
                recyclerView,
                adapter,
                loadingContentHandler
        )
        lazyLoadingHandler.listener = this

    }

    override fun resume() {
        super.resume()

        lazyLoadingHandler.onResume()
    }

    override fun retrieveNextData(page: Int) {

        APIManager.call(
                requireContext(),
                {
                    it.getNewsList(
                            page,
                            30
                    )
                },
                true,
                false,
                { b ->

                    val newItems = b.getNews().map {
                        ArticleSummary(it)
                    }

                    lazyLoadingHandler.onRetrievingDataSuccess(newItems, newItems.size, b.totalPages)
                },
                {
                    lazyLoadingHandler.onRetrievingDataError()
                }
        )

    }

}
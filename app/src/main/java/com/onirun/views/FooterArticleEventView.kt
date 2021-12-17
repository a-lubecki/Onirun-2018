package com.onirun.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.onirun.R
import kotlinx.android.synthetic.main.footer_article_event.view.*


class FooterArticleEventView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {


    private var title = ""
    private var clickListener: ((View?) -> Unit)? = null

    init {

        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.footer_article_event, this, true)

        updateUI()
    }

    fun setEvent(title: String, clickListener: ((View?) -> Unit)?) {

        this.title = title
        this.clickListener = clickListener

        updateUI()
    }

    fun updateUI() {

        textViewName.text = title

        buttonShowEvent.setOnClickListener(clickListener)
    }

}
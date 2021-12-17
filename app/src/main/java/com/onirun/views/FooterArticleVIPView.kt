package com.onirun.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.onirun.R
import com.onirun.model.Article
import com.onirun.utils.setVisible
import com.onirun.utils.share
import kotlinx.android.synthetic.main.footer_article_vip.view.*


class FooterArticleVIPView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {


    private var specificVIP: Article.ArticleSpecificVIP? = null


    init {

        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.footer_article_vip, this, true)

        updateUI()
    }

    fun setVIP(specificVIP: Article.ArticleSpecificVIP?) {

        this.specificVIP = specificVIP

        updateUI()
    }

    fun updateUI() {

        val specificVIP = specificVIP ?: return

        avatar.setCustomName(specificVIP.name)

        textViewUserName.text = specificVIP.name

        updateButton(buttonVIPFacebook, specificVIP.facebook)
        updateButton(buttonVIPInstagram, specificVIP.instagram)
        updateButton(buttonVIPTwitter, specificVIP.twitter)
        updateButton(buttonVIPStrava, specificVIP.strava)
    }

    private fun updateButton(button: ImageButton, url: String) {

        if (url.isEmpty()) {

            button.setVisible(false)

        } else {

            button.setVisible(true)

            button.setOnClickListener {
                url.share { intent ->
                    context.startActivity(intent)
                }
            }
        }
    }

}
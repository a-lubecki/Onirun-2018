package com.onirun.screens.event

import android.content.Context
import android.os.Bundle
import com.bumptech.glide.Glide
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.screens.main.BaseAnimatedActivity
import com.onirun.utils.start
import kotlinx.android.synthetic.main.activity_event_illustration.*


class EventIllustrationActivity : BaseAnimatedActivity() {


    companion object {

        const val EXTRA_ILLUSTRATION = "illustration"

        fun start(context: Context, illustration: String) {

            context.newIntent<EventIllustrationActivity>()
                    .putExtra(EXTRA_ILLUSTRATION, illustration)
                    .start(context)
        }
    }


    override val layoutId = R.layout.activity_event_illustration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Glide.with(this)
                .load(APIManager.newAuthorizedImageUrl(intent.getStringExtra(EXTRA_ILLUSTRATION)))
                .into(imageViewIllustration)
    }

}
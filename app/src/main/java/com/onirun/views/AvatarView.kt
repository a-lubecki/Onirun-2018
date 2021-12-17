package com.onirun.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.mcxiaoke.koi.ext.dpToPx
import com.mcxiaoke.koi.log.logw
import com.onirun.R
import com.onirun.api.UserManager
import com.onirun.utils.setVisible
import kotlinx.android.synthetic.main.view_avatar.view.*


class AvatarView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {


    var isSizeBig = false
        private set

    private var customName = ""


    init {

        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_avatar, this, true)

        context.obtainStyledAttributes(attrs, R.styleable.AvatarView)?.apply {
            isSizeBig = getBoolean(R.styleable.AvatarView_isSizeBig, false)
            recycle()
        }

        updateUI()
    }

    fun setCustomName(name: String) {

        customName = name

        updateUI()
    }

    fun updateUI() {

        var letter = customName
        if (letter.isBlank()) {
            letter = UserManager.getUserName(context)
        }

        if (letter.isNotEmpty()) {

            try {
                letter = letter.first { it.isLetterOrDigit() }.toString().toUpperCase()
            } catch (e: NoSuchElementException) {
                logw("AvatarView", e)
            }
        }

        val isLetterVisible = letter.isNotBlank()

        imageViewAvatar.apply {

            setImageResource(if (isSizeBig) R.drawable.avatar_placeholder_100 else R.drawable.avatar_placeholder_40)

            visibility = if (isLetterVisible) {
                INVISIBLE
            } else {
                VISIBLE
            }
        }

        layoutContainer.apply {

            setBackgroundResource(if (isSizeBig) R.drawable.avatar_background_100 else R.drawable.avatar_background_40)

            setVisible(isLetterVisible)
        }

        if (isLetterVisible) {

            textViewLetter.apply {

                text = letter

                textSize = if (isSizeBig) {
                    66f
                } else {
                    26f
                }

                (layoutParams as? FrameLayout.LayoutParams)?.topMargin = if (isSizeBig) {
                    -8
                } else {
                    -4
                }.dpToPx()
            }
        }

    }

}
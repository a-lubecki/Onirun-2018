package com.onirun.screens.account

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import com.mcxiaoke.koi.ext.dpToPx
import com.mcxiaoke.koi.ext.inflate
import com.onirun.R
import com.onirun.utils.setVisible


class RowProfileQuestion(context: Context) : LinearLayout(context), View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private val layoutAnswers by lazy {
        findViewById<LinearLayout>(R.id.layoutAnswers)
    }

    //displayed only if the screen is too small to show 4 answers on one line
    private val layoutAnswersOverflow by lazy {
        findViewById<LinearLayout>(R.id.layoutAnswersOverflow)
    }

    private val textViewQuestion by lazy {
        findViewById<TextView>(R.id.textViewQuestion)
    }

    lateinit var question: ProfileQuestion
        private set
    var value: Any? = null
        private set

    private lateinit var clickListener: (question: RowProfileQuestion, value: Any) -> Unit


    init {
        inflate(context, R.layout.row_profile_question, this)
    }

    private fun getToggleButton(layoutButton: ViewGroup): ToggleButton {
        return layoutButton.findViewById(R.id.button)
    }

    fun updateQuestion(question: ProfileQuestion, clickListener: (question: RowProfileQuestion, value: Any) -> Unit) {

        this.question = question

        textViewQuestion.setText(question.questionResId)

        //get screen size to know the answers repartition between the 2 layouts
        val isSmallScreen = isSmallScreenForAnswers(question.answersResId.size)
        layoutAnswersOverflow.setVisible(isSmallScreen)

        //remove all previous buttons before adding the new ones
        layoutAnswers.removeAllViews()
        layoutAnswersOverflow.removeAllViews()

        //merge the answers texts and values to iterate on both
        question.answersResId.zip(question.answersValue).forEachIndexed { i, answer ->

            //create and add a new toggle button
            val layoutButton = context.inflate(R.layout.button_profile_question) as ViewGroup
            val button = getToggleButton(layoutButton)

            val str = context.getString(answer.first)
            button.text = str
            button.textOn = str
            button.textOff = str

            if (question.isEmoji) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            }

            //set the value in the tag to propagate it on click
            button.tag = answer.second
            button.setOnClickListener(this)

            button.setOnCheckedChangeListener(this)

            //if the screen is too small for 4 answers, put 2 answers by layouts
            if (isSmallScreen && i >= 2) {
                layoutAnswersOverflow.addView(layoutButton)
            } else {
                layoutAnswers.addView(layoutButton)
            }
        }

        this.clickListener = clickListener
    }

    private fun isSmallScreenForAnswers(nbAnswers: Int): Boolean {
        //every answers are 76dp : 8 + 60 + 8
        return nbAnswers * 110.dpToPx() > Resources.getSystem().displayMetrics.widthPixels
    }

    fun updateValue(value: Any) {

        this.value = value

        updateToggleButtons(layoutAnswers)

        updateToggleButtons(layoutAnswersOverflow)
    }

    private fun updateToggleButtons(layout: ViewGroup) {

        for (i in 0 until layout.childCount) {

            val layoutButton = layout.getChildAt(i) as? ViewGroup ?: continue

            val button = getToggleButton(layoutButton)

            //disable the button to avoid recursive calls
            button.setOnCheckedChangeListener(null)
            button.isChecked = button.tag == value
            button.setOnCheckedChangeListener(this)
        }
    }

    override fun onClick(v: View?) {

        if (v == null) {
            return
        }

        value = v.tag

        clickListener.invoke(this, value!!)
    }

    override fun onCheckedChanged(button: CompoundButton?, checked: Boolean) {

        button?.let {
            updateValue(it.tag)
        }
    }

}
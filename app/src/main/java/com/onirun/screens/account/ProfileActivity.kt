package com.onirun.screens.account

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import com.mcxiaoke.koi.ext.newIntent
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.model.RunnerProfile
import com.onirun.model.bundle.BundleRunnerProfile
import com.onirun.screens.onboarding.LaunchManager
import com.onirun.screens.settings.SettingsLocationActivity
import com.onirun.utils.isVisible
import com.onirun.utils.setVisible
import com.onirun.utils.startForResult
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity : BaseAccountActivity() {


    companion object {

        fun start(context: Context, isFinishingLogin: Boolean) {
            startInternal(context, ProfileActivity::class.java, false, isFinishingLogin)
        }

        fun startForResultApiReload(activity: Activity) {

            activity.newIntent<ProfileActivity>()
                    .startForResult(activity, REQUEST_CODE_API_RELOAD)
        }
    }


    override val layoutId = R.layout.activity_profile

    private var currentProfile: RunnerProfile? = null
    private var bundleProfileBuilder = BundleRunnerProfile.Builder()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //init questions views
        ProfileQuestion.values().forEach { question ->

            val rowQuestion = RowProfileQuestion(this)

            //add listener for answer click
            rowQuestion.updateQuestion(question) { row, value ->

                row.question.insertValueToBundle(bundleProfileBuilder, value)

                //show next question
                revealNextQuestion(row)
            }

            layoutQuestions.addView(rowQuestion)

            //hide the question until the data retrieving has been done
            rowQuestion.setVisible(false)
        }

        layoutQClubName.setVisible(false)
    }

    override fun retrieveData() {

        APIManager.call(
                this,
                {
                    it.getRunnerProfile()
                },
                true,
                false,
                { b ->

                    currentProfile = RunnerProfile(b)

                    bundleProfileBuilder.setGender(currentProfile!!.gender)
                    bundleProfileBuilder.setNbYears(currentProfile!!.nbYears)
                    bundleProfileBuilder.setNbTrainings(currentProfile!!.nbTrainings)
                    bundleProfileBuilder.setRanSomeRaces(currentProfile!!.ranSomeRaces)
                    bundleProfileBuilder.setNbRacesThisYear(currentProfile!!.nbRacesThisYear)
                    bundleProfileBuilder.setRaceCategory(currentProfile!!.raceCategory?.jsonValue)
                    bundleProfileBuilder.setIsInClub(currentProfile!!.isInClub)
                    bundleProfileBuilder.setClubName(currentProfile!!.clubName)

                    updateUI()

                    //show the first question if hidden
                    showFirstQuestion()
                },
                {

                    if (LaunchManager.isOnboardingPassed(this)) {

                        finish()

                    } else {

                        //don't finish the screen during onboarding to not block the user
                        showFirstQuestion()
                    }
                }
        )

    }

    private fun updateUI() {

        if (currentProfile == null) {
            return
        }

        textViewGrade.text = currentProfile!!.grade?.name ?: ""

        var previousRow: RowProfileQuestion? = null

        //fill each question with profile answers
        for (i in 0 until layoutQuestions.childCount) {

            val row = layoutQuestions.getChildAt(i) as? RowProfileQuestion ?: continue

            val value = row.question.extractValueFromProfile.invoke(currentProfile!!)

            if (value == null) {

                if (previousRow?.value != null) {
                    //show the value if the user has not answered this question but the previous
                    row.setVisible(true)
                } else {
                    //hide the value if user has not answered the question
                    row.setVisible(false)
                }

            } else {

                //show the question only if the user has already answered it yet
                row.setVisible(true)
                row.updateValue(value)
            }

            previousRow = row
        }

        //hide the other questions if didn't ran some races, but show the next isInClub question
        if (currentProfile!!.ranSomeRaces != null) {

            if (currentProfile!!.ranSomeRaces == true) {

                setQuestionVisible(
                        ProfileQuestion.RACE_CATEGORY,
                        currentProfile!!.nbRacesThisYear != null,
                        false
                )

                setQuestionVisible(
                        ProfileQuestion.IS_IN_CLUB,
                        currentProfile!!.ranSomeRaces != null,
                        false
                )

            } else {

                setQuestionVisible(ProfileQuestion.NB_RACES_THIS_YEAR, false, false)
                setQuestionVisible(ProfileQuestion.RACE_CATEGORY, false, false)
                setQuestionVisible(ProfileQuestion.IS_IN_CLUB, true, false)
            }
        }

        if (currentProfile!!.isInClub == true) {
            //show the edittext if the user has answered this one or the previous "is in club?" question
            editTextClubName.setText(currentProfile!!.clubName, TextView.BufferType.EDITABLE)
            layoutQClubName.setVisible(true)
        }
    }

    private fun revealNextQuestion(currentRow: RowProfileQuestion) {

        val currentQuestion = currentRow.question
        val nextRow = findNextRow(currentQuestion)
        val nextRowLayout = nextRow ?: layoutQClubName

        //check visibility before updating the next question visibility
        val isLastQuestionVisible = isLastQuestionVisible()

        //special cases
        when (currentQuestion) {

            ProfileQuestion.RAN_SOME_RACES -> {
                //special case for the question ran a race ?

                if (currentRow.value == true) {

                    val rowNbRaces = findRow(ProfileQuestion.NB_RACES_THIS_YEAR)
                    setRowVisible(rowNbRaces, true, true)

                    if (rowNbRaces.value != null) {
                        setQuestionVisible(ProfileQuestion.RACE_CATEGORY, true, true)
                    } else {
                        if (findRow(ProfileQuestion.RACE_CATEGORY).value != null) {
                            setQuestionVisible(ProfileQuestion.IS_IN_CLUB, true, true)
                        }
                    }

                } else {

                    setQuestionVisible(ProfileQuestion.NB_RACES_THIS_YEAR, false, true)
                    setQuestionVisible(ProfileQuestion.RACE_CATEGORY, false, true)
                    setQuestionVisible(ProfileQuestion.IS_IN_CLUB, true, true)
                }
            }

            ProfileQuestion.NB_RACES_THIS_YEAR -> {
                //special case for the question ran a race ?

                if (currentRow.value != null) {

                    val rowRaceCategory = findRow(ProfileQuestion.RACE_CATEGORY)
                    setRowVisible(rowRaceCategory, true, true)

                    if (rowRaceCategory.value != null) {
                        setQuestionVisible(ProfileQuestion.IS_IN_CLUB, true, true)
                    }

                } else {

                    setQuestionVisible(ProfileQuestion.RACE_CATEGORY, false, true)
                    setQuestionVisible(ProfileQuestion.IS_IN_CLUB, true, true)
                }
            }

            ProfileQuestion.IS_IN_CLUB -> {

                //display club name only if is in club
                setRowVisible(nextRowLayout, currentRow.value == true, false)
            }

            //display next row normally
            else -> setRowVisible(nextRowLayout, true, true)
        }

        //scroll to reveal the next question if it's not the last
        if (!isLastQuestionVisible) {

            scrollView.post {
                scrollView.smoothScrollTo(0, Int.MAX_VALUE)
            }
        }
    }

    private fun isLastQuestionVisible(): Boolean {

        if (findRow(ProfileQuestion.IS_IN_CLUB).value == false) {
            return true
        }

        return layoutQClubName.isVisible()
    }

    private fun showFirstQuestion() {
        setQuestionVisible(ProfileQuestion.values()[0], true, false)
    }

    private fun setQuestionVisible(question: ProfileQuestion, visible: Boolean, animated: Boolean) {

        setRowVisible(findRow(question), visible, animated)
    }

    private fun setRowVisible(layoutRow: ViewGroup, visible: Boolean, animated: Boolean) {

        if (layoutRow.isVisible() == visible) {
            //no changes
            return
        }

        if (!animated) {
            layoutRow.setVisible(visible)
            return
        }

        //else animate:

        if (visible) {
            //start showing
            layoutRow.setVisible(true)
        }

        val nextAlpha = if (visible) 1f else 0f
        layoutRow.animate().alpha(nextAlpha).setListener(object : Animator.AnimatorListener {

            override fun onAnimationRepeat(p0: Animator?) {
                //do nothing
            }

            override fun onAnimationEnd(p0: Animator?) {

                if (!visible) {
                    //end hiding
                    layoutRow.setVisible(false)
                }
            }

            override fun onAnimationCancel(p0: Animator?) {
                //do nothing
            }

            override fun onAnimationStart(p0: Animator?) {
                //do nothing
            }
        })

    }

    private fun findNextQuestion(question: ProfileQuestion): ProfileQuestion? {

        ProfileQuestion.values().let { values ->

            values.indexOf(question).let { pos ->

                if (pos + 1 < values.size) {
                    //found
                    return values[pos + 1]
                }
            }
        }

        return null
    }

    private fun findRow(question: ProfileQuestion): RowProfileQuestion {

        for (i in 0 until layoutQuestions.childCount) {

            val row = layoutQuestions.getChildAt(i) as? RowProfileQuestion ?: continue

            if (row.question == question) {
                //found
                return row
            }
        }

        throw UnsupportedOperationException()
    }

    private fun findNextRow(question: ProfileQuestion): RowProfileQuestion? {

        val nextRow = findNextQuestion(question) ?: return null

        return findRow(nextRow)
    }

    override fun sendData() {

        //update club name in builder if it has changed
        val newClubName = editTextClubName.text?.toString() ?: ""

        if (currentProfile == null || currentProfile!!.clubName.isEmpty()) {
            if (newClubName.isNotEmpty()) {
                bundleProfileBuilder.setClubName(newClubName)
            }
        } else {
            if (currentProfile!!.clubName != newClubName) {
                bundleProfileBuilder.setClubName(newClubName)
            }
        }

        APIManager.call(
                this,
                {
                    it.putRunnerProfile(bundleProfileBuilder.build())
                },
                true,
                false,
                {
                    onSendDataSuccess()
                },
                {
                    onSendDataError()
                }
        )

    }

    override fun startNextOnboardingActivity() {

        if (LaunchManager.isOnboardingPassed(this)) {
            LaunchManager.startMainActivity(this)
            return
        }

        SettingsLocationActivity.start(this, true, isFinishingLogin)
    }

}
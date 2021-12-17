package com.onirun.recyclerview

import android.animation.Animator
import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.github.nitrico.lastadapter.LastAdapter
import com.github.nitrico.lastadapter.Type
import com.mcxiaoke.koi.ext.dpToPx
import com.onirun.BR
import com.onirun.R
import com.onirun.databinding.RowRunnerEventBinding
import com.onirun.model.RaceEngagement
import com.onirun.model.RaceFormat
import com.onirun.model.RunnerEvent
import com.onirun.model.bundle.BundleRunnerEventPage
import com.onirun.screens.event.EventActivity
import com.onirun.utils.LoadingContentHandler
import com.onirun.utils.isVisible
import com.onirun.utils.setVisible
import java.util.*


/**
 * Created by Aurelien Lubecki
 * on 01/05/2018.
 */
class RunnerEventListHandler(items: MutableList<Any>,
                             recyclerView: RecyclerView,
                             loadingContentHandler: LoadingContentHandler?,
                             private val runnerEventListener: RunnerEventListHandlerListener,
                             private val buttonFilter: Button?)
    : LazyLoadingActivityHandler(items, recyclerView, newAdapter(items, recyclerView), loadingContentHandler), LazyLoadingListHandlerListener, Animator.AnimatorListener {


    companion object {

        fun newAdapter(items: MutableList<Any>, recyclerView: RecyclerView): LastAdapter {

            val context = recyclerView.context

            return LastAdapter(items, BR.item)
                    .into(recyclerView)
                    .map<LazyLoadingFooter>(R.layout.row_loader)
                    .map<RaceSectionHeader>(R.layout.row_race_section_header)
                    .map<RunnerEvent>(Type<RowRunnerEventBinding>(R.layout.row_runner_event)
                            .onBind {

                                val event = it.binding.item ?: return@onBind

                                completeRunnerEventBind(it.binding, event)
                            }
                            .onClick {

                                val event = it.binding.item ?: return@onClick

                                EventActivity.startForResultApiReload(context as Activity, event.eventId)
                            }
                    )
        }

        fun completeRunnerEventBind(binding: RowRunnerEventBinding, event: RunnerEvent) {

            binding.textViewChallenge.setVisible(event.challenge)

            binding.imageViewHasFriends.setImageResource(when (event.nbFriends) {
                0 -> R.drawable.friend_no
                else -> R.drawable.friend_yes
            })

            binding.imageViewEngagement.setImageResource(when (event.engagement) {
                RaceEngagement.PARTIALLY_ENGAGED -> R.drawable.run_standby
                RaceEngagement.FULLY_ENGAGED -> R.drawable.run_yes
                else -> R.drawable.run_no
            })

            fillFormatsEmojis(binding.layoutFormats, event.raceFormats, 26)
        }

        fun getCurrentDateWithoutTime(): Date {

            val gc = GregorianCalendar(Locale.FRANCE)
            gc.time = Date()

            gc.set(Calendar.HOUR_OF_DAY, 0)
            gc.set(Calendar.MINUTE, 0)
            gc.set(Calendar.SECOND, 0)
            gc.set(Calendar.MILLISECOND, 0)

            return Date(gc.timeInMillis)
        }

        fun getLastDayOfNextMonths(nbMonths: Int): Date {

            if (nbMonths <= 0) {
                throw IllegalArgumentException()
            }

            val gc = GregorianCalendar(Locale.FRANCE)
            gc.time = Date()

            gc.add(Calendar.MONTH, 1 + nbMonths)

            gc.set(Calendar.DAY_OF_MONTH, 1)
            gc.set(Calendar.HOUR_OF_DAY, 0)
            gc.set(Calendar.MINUTE, 0)
            gc.set(Calendar.SECOND, 0)
            gc.set(Calendar.MILLISECOND, 0)

            gc.add(Calendar.MILLISECOND, -1)

            return Date(gc.timeInMillis)
        }

        fun fillFormatsEmojis(layout: LinearLayout, formats: Collection<RaceFormat>, sizeInDp: Int) {

            layout.removeAllViews()

            val context = layout.context
            val sizeValue = sizeInDp.dpToPx()

            formats.forEach { format ->

                val imageView = ImageView(context)
                imageView.setImageBitmap(format.emoji)

                val params = LinearLayout.LayoutParams(sizeValue, sizeValue)
                imageView.layoutParams = params

                layout.addView(imageView)
            }

        }

        fun manageLongDescription(description: String, textViewDescription: TextView, buttonSeeMore: Button, maxLines: Int) {

            textViewDescription.text = description
            textViewDescription.setVisible(description.isNotEmpty())

            buttonSeeMore.setVisible(false)
            buttonSeeMore.setOnClickListener(null)

            //show the more button if the text has been ellipsized
            textViewDescription.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {

                override fun onGlobalLayout() {

                    if (textViewDescription.layout == null) {
                        //wait for layout to be ready to count nb of lines
                        return
                    }

                    textViewDescription.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val lineCount = textViewDescription.layout.lineCount

                    if (lineCount <= maxLines) {
                        //no need to ellipsize
                        return
                    }

                    textViewDescription.maxLines = maxLines - 2 //height of the button
                    buttonSeeMore.setVisible(true)

                    buttonSeeMore.setOnClickListener {

                        textViewDescription.maxLines = Int.MAX_VALUE

                        buttonSeeMore.setVisible(false)
                        buttonSeeMore.setOnClickListener(null)
                    }
                }
            })
        }
    }


    init {

        listener = this

        buttonFilter?.let {

            it.setVisible(false)

            //show hide the filter button when the list is scrolling
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!recyclerView.isVisible()) {
                        return
                    }

                    if (dy < 0) {

                        showButton()

                    } else if (dy > 0) {

                        hideButton()

                    }

                }
            })

        }

    }

    fun onRetrievingRunnerEventsSuccess(response: BundleRunnerEventPage) {

        //add months / races sections to recycler view
        var lastSectionHeaderMonth: Date? = null

        if (!isLoadingFirstPage()) {
            lastSectionHeaderMonth = (items.lastOrNull {
                it is RaceSectionHeader
            } as? RaceSectionHeader)?.date
        }

        val events = extractEventsSections(response, lastSectionHeaderMonth)
        onRetrievingDataSuccess(events, events.size, response.totalPages)

        buttonFilter?.setVisible(true)
    }

    private fun extractEventsSections(response: BundleRunnerEventPage, lastSectionHeaderMonth: Date?): List<Any> {

        val sectionItems = mutableListOf<Any>()

        response.getContent().forEach { bundle ->

            val eventsOfMonth = bundle.getEvents().map { bundleEvent ->
                RunnerEvent(bundleEvent)
            }

            if (eventsOfMonth.isEmpty()) {
                //don't display header if there are no elements
                return@forEach
            }

            val currentMonth = eventsOfMonth.first().monthOfDateBegin

            //add a header before the races only if the races are not part of the previously loaded section
            if (lastSectionHeaderMonth == null || lastSectionHeaderMonth != currentMonth) {
                sectionItems.add(RaceSectionHeader(currentMonth, bundle.total))
            }

            sectionItems.addAll(eventsOfMonth)
        }

        return sectionItems
    }

    override fun retrieveNextData(page: Int) {

        runnerEventListener.retrieveNextRunnerEvents(page)
    }

    private fun showButton() {

        if (buttonFilter!!.isVisible()) {
            return
        }

        buttonFilter.setVisible(true)

        buttonFilter.animate()
                .alpha(1f)
                .setDuration(300)
    }

    private fun hideButton() {

        if (!buttonFilter!!.isVisible()) {
            return
        }

        buttonFilter.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(this)
    }

    override fun onAnimationStart(animation: Animator?) {
        //do nothing
    }

    override fun onAnimationRepeat(animation: Animator?) {
        //do nothing
    }

    override fun onAnimationEnd(animation: Animator?) {

        if (buttonFilter!!.alpha <= 0) {
            buttonFilter.setVisible(false)
        }
    }

    override fun onAnimationCancel(animation: Animator?) {
        //do nothing
    }

}

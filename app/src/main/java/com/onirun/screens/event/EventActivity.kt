package com.onirun.screens.event

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.mcxiaoke.koi.ext.Bundle
import com.mcxiaoke.koi.ext.dpToPx
import com.mcxiaoke.koi.ext.newIntent
import com.mcxiaoke.koi.ext.onClick
import com.onirun.R
import com.onirun.api.APIManager
import com.onirun.model.Event
import com.onirun.model.RaceRegistration
import com.onirun.model.bundle.BundleEventAndRegistrations
import com.onirun.recyclerview.RunnerEventListHandler
import com.onirun.screens.main.BaseLoadingActivity
import com.onirun.utils.setVisible
import com.onirun.utils.start
import com.onirun.utils.startForResult
import com.onirun.utils.trackEvent
import kotlinx.android.synthetic.main.activity_event.*


/**
 * Created by Aurelien Lubecki
 * on 21/03/2018.
 */
class EventActivity : BaseLoadingActivity() {


    companion object {

        const val EXTRA_EVENT_ID = "eventId"

        fun newIntent(context: Context, eventId: Int): Intent {

            return context.newIntent<EventActivity>()
                    .putExtra(EXTRA_EVENT_ID, eventId)
        }

        fun startForResultApiReload(activity: Activity, eventId: Int) {

            newIntent(activity, eventId)
                    .startForResult(activity, REQUEST_CODE_API_RELOAD)
        }
    }


    override val layoutId = R.layout.activity_event

    private lateinit var event: Event
    private var registrations: List<RaceRegistration>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set a small app bar
        initAppBar(null)

        retrieveEvent()
    }

    override fun onAPIReloadRequested(data: Intent?) {
        super.onAPIReloadRequested(data)

        updateEvent()
    }

    fun updateEvent() {

        //the event has changed : update the previous activity
        setResultAPIReload()

        retrieveEvent()
    }

    private fun retrieveEvent() {

        showLoaderDelayed()

        registrations = null

        setContentVisible(false)

        APIManager.call(
                this,
                {
                    it.getRunnerEvent(
                            intent.getIntExtra(EXTRA_EVENT_ID, 0)
                    )
                },
                true,
                false,
                {

                    hideLoader()

                    displayEvent(it)
                },
                {
                    finish()
                }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun displayEvent(response: BundleEventAndRegistrations) {

        event = Event(response.event!!)
        registrations = response.getRegistrations().map {
            RaceRegistration(it)
        }

        setContentVisible(true)

        initMap()
        initIllustration()
        initAppBar(event)

        textViewEventName.text = event.name

        textViewDate.text = event.getDisplayableLongDate()

        textViewCity.text = "${event.city} (${event.department.code})"
        textViewCity.onClick {
            openGoogleMaps()
        }

        layoutNotValidated.setVisible(!event.isDateValidated())
        textViewNotValidated.text = getString(R.string.event_box_not_validated, event.getDisplayableYear())

        buttonSeeMore.setVisible(false)

        val description = if (event.description.isNotEmpty()) event.description else getString(R.string.event_description_placeholder)
        RunnerEventListHandler.manageLongDescription(description, textViewDescription, buttonSeeMore, 5)

        //fill layout with races
        layoutRaces.removeAllViews()

        event.races.forEach { race ->

            val row = RowRace(this)
            row.setRace(event, race, registrations?.find { it.raceId == race.raceId })

            layoutRaces.addView(row)
        }

        textViewContactTitle.text = event.name

        var hasContact = false

        if (event.urlPayment.isNotEmpty()) {
            hasContact = true
        } else {
            layoutInscription.setVisible(false)
        }

        if (event.urlWebsite.isNotEmpty()) {
            hasContact = true
        } else {
            layoutWebsite.setVisible(false)
        }

        if (event.urlFacebook.isNotEmpty()) {
            hasContact = true
        } else {
            layoutFacebook.setVisible(false)
        }

        if (event.urlTwitter.isNotEmpty()) {
            hasContact = true
        } else {
            layoutTwitter.setVisible(false)
        }

        if (event.urlInstagram.isNotEmpty()) {
            hasContact = true
        } else {
            layoutInstagram.setVisible(false)
        }

        if (event.mail.isNotEmpty()) {
            hasContact = true
        } else {
            layoutEmail.setVisible(false)
        }

        if (event.phone.isNotEmpty()) {
            hasContact = true
        } else {
            layoutPhone.setVisible(false)
        }

        if (!hasContact) {
            layoutContact.setVisible(false)
        }

        layoutInscription.setOnClickListener {

            trackOpenExtraLink("online_subscription")

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.urlPayment))
            startActivity(intent)
        }

        layoutWebsite.setOnClickListener {

            trackOpenExtraLink("website")

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.urlWebsite))
            startActivity(intent)
        }

        layoutFacebook.setOnClickListener {

            trackOpenExtraLink("facebook")

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.urlFacebook))
            startActivity(intent)
        }

        layoutTwitter.setOnClickListener {

            trackOpenExtraLink("twitter")

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.urlTwitter))
            startActivity(intent)
        }

        layoutInstagram.setOnClickListener {

            trackOpenExtraLink("instagram")

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.urlInstagram))
            startActivity(intent)
        }

        layoutEmail.setOnClickListener {

            trackOpenExtraLink("email")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("mailto:?to=" + event.mail)
            }

            startActivity(intent)
        }

        layoutPhone.setOnClickListener {

            trackOpenExtraLink("phone")

            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:" + event.phone)
            }

            startActivity(intent)
        }

    }

    private fun trackOpenExtraLink(type: String) {

        trackEvent("open_extra_link_btn", Bundle {
            putString("link_type", type)
            putInt("id_event", event.eventId)
        })
    }

    private fun initMap() {

        val location = event.location

        if (location == null) {

            layoutMapContainer.setVisible(false)
            return
        }

        //init google map async to avoid lag before starting the activity
        layoutContent.handler?.postDelayed({

            if (!isActivityValid()) {
                //don't load map fragment if the activity is finishing to not cause a crash
                return@postDelayed
            }

            val fragmentMap = SupportMapFragment.newInstance()
            supportFragmentManager.beginTransaction().replace(R.id.layoutMapContainer, fragmentMap).commitAllowingStateLoss()

            fragmentMap.getMapAsync { googleMap ->

                //set the same color as the background
                fragmentMap.view?.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundDefault))

                googleMap.uiSettings.setAllGesturesEnabled(false)
                googleMap.uiSettings.isMapToolbarEnabled = false

                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_dark))

                val marker = MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.place_40))
                        .position(location)

                googleMap.addMarker(marker)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14f))

                //add 2 click listeners to handle click on all the map
                googleMap.setOnMapClickListener {
                    openGoogleMaps()
                }
                googleMap.setOnMarkerClickListener {

                    openGoogleMaps()

                    true
                }

                //show the google logo
                googleMap.setPadding(0, 0, 0, 50.dpToPx())

                //fix a glitch with white google maps colors during loading by displaying the map after a delay
                fragmentMap.view?.let { map ->

                    map.alpha = 0f

                    layoutContent.handler?.postDelayed({
                        map.animate().alpha(1f)
                    }, 1000)
                }
            }

        }, 500)

    }

    private fun initIllustration() {

        if (!event.hasIllustration()) {

            layoutIllustration.setVisible(false)
            return
        }

        layoutIllustration.setVisible(true)

        val sizeInPx = 120.dpToPx()

        Glide.with(this)
                .load(APIManager.newAuthorizedImageUrl(event.illustration))
                .apply(RequestOptions()
                        .override(sizeInPx, sizeInPx)
                        .transform(CircleCrop())
                )
                .into(imageViewIllustration)

        layoutIllustration.setOnClickListener {

            if (!event.hasIllustration()) {
                return@setOnClickListener
            }

            EventIllustrationActivity.start(this, event.illustration)
        }
    }

    private fun initAppBar(event: Event?) {

        val appBarHeight: Int

        if (event?.location != null) {

            appBarHeight = if (event.hasIllustration()) {
                //map + image
                350
            } else {
                //map only
                250
            }

        } else {

            appBarHeight = if (event != null && event.hasIllustration()) {
                //illustration only
                200
            } else {
                //no map + no illustration
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        appBar.layoutParams.height = appBarHeight.dpToPx()

        if (event != null) {

            val mapMarginBottom = if (event.location != null && !event.hasIllustration()) {
                //map only
                0
            } else {
                //others
                50
            }

            (layoutMapContainer.layoutParams as RelativeLayout.LayoutParams).bottomMargin = mapMarginBottom.dpToPx()
        }
    }

    private fun setContentVisible(visible: Boolean) {

        layoutAppBarContent.setVisible(visible)
        layoutContent.setVisible(visible)
    }

    private fun openGoogleMaps() {

        event.location?.let {

            //to know more => https://developers.google.com/maps/documentation/urls/guide
            val mapsUrl = Uri.Builder()
                    .scheme("https")
                    .authority("www.google.com")
                    .path("maps/dir/")
                    .appendQueryParameter("api", "1")
                    .appendQueryParameter("destination", "${it.latitude},${it.longitude}")
                    .appendQueryParameter("travelmode", "driving")
                    .build()

            Intent(Intent.ACTION_VIEW, mapsUrl).start(this)
        }
    }

}
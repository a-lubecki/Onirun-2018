package com.onirun.model.bundle

import com.google.android.gms.maps.model.LatLng

/**
 * A data class used to extract data from the result of the API.
 * All the fields in the constructor must have a default value (even the nullable fields with a default null value).
 *
 * Created by Aurelien Lubecki
 * on 23/04/2018.
 */
data class BundleLatLng(val lat: Double, val lng: Double) {

    fun newLatLng(): LatLng {
        return LatLng(lat, lng)
    }
}
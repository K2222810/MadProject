// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.madproject

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.gms.common.api.Status

class MapsMarker : FragmentActivity(), OnMapReadyCallback {
    private lateinit var placesClient: PlacesClient
    private var googleMap: GoogleMap? = null

    companion object {
        private const val TAG = "MapsMarker"
        private const val API_KEY = "YOUR_API_KEY" // Replace with your actual API key
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Places
        Places.initialize(applicationContext, API_KEY)
        placesClient = Places.createClient(this)

        // Set the content view
        setContentView(R.layout.activity_maps)

        // Setup map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Setup Autocomplete
        setupAutocompletePlaces()
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Default location (Sydney)
        val sydney = LatLng(-33.852, 151.211)
        map.addMarker(
            MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))
    }
    private fun setupAutocompletePlaces() {
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(R.id.autocomplete_fragment) as? AutocompleteSupportFragment

        autocompleteFragment?.let { fragment ->
            // Specify the types of place data to return
            fragment.setPlaceFields(
                listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG
                )
            )

            // Customize the input field
            fragment.setHint("Search for a location")

            // Get the EditText inside the fragment
            val editText = fragment.view?.findViewById<EditText>(
                com.google.android.libraries.places.R.id.places_autocomplete_search_input
            )

            editText?.let { input ->
                // Clear default background to allow interaction
                input.background = null

                // Prevent the input from losing focus quickly
                input.setOnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        // Keep the input focused
                        view.requestFocus()
                    }
                }

                // Add a touch listener to ensure interaction
                input.setOnTouchListener { view, event ->
                    view.performClick()
                    false
                }
            }

            // Set up place selection listener
            fragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
                override fun onPlaceSelected(place: Place) {
                    Log.i(TAG, "Place: ${place.name}, ${place.id}")

                    place.latLng?.let { latLng ->
                        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                        googleMap?.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(place.name)
                        )
                    }
                }

                override fun onError(status: Status) {
                    Log.e(TAG, "An error occurred: $status")
                }
            })
        }
    }
}

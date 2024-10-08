package com.example.weather.features.map.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.weather.R
import com.example.weather.databinding.ActivityMapBinding
import com.example.weather.utils.managers.SharedDataManager
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.enums.Language
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class Map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private var map: GoogleMap? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedMarker: Marker? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        lifecycleScope.launch {
            val language = SharedDataManager.languageFlow.first() // Collect the latest value
            Log.i("DEBUGG", "Latest language: $language")
            when (language) {
                Language.ENGLISH -> updateLocale("en")
                Language.ARABIC -> updateLocale("ar")
            }
        }
    }



    private fun updateLocale(language:String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources: Resources = this.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        applicationContext.createConfigurationContext(config)
        applicationContext.resources.updateConfiguration(config, applicationContext.resources.displayMetrics)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key), Locale.US)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        requestLocationPermission()

        lifecycleScope.launch {
            SharedDataManager.languageFlow.collect { language ->
                Log.i("DEBGUGG", "languageDetails: $language")
                when (language) {
                    Language.ENGLISH -> updateLocale("en")
                    Language.ARABIC -> updateLocale("ar")
                }
            }
        }

        binding.etSearchLocation.apply {
            val geocoder = Geocoder(this@Map, Locale.getDefault())
            val adapter = GeocoderAutoCompleteAdapter(this@Map, geocoder)
            setAdapter(adapter)

            setOnItemClickListener { _, _, position, _ ->
                val selectedLocation = adapter.getItem(position)
                selectedLocation?.let {
                    searchLocationByGeocoder(it)
                }
            }
        }


        binding.btnGetWeather.setOnClickListener {
            selectedMarker?.let {
                val resultIntent = Intent().apply {
                    putExtra(Keys.LATITUDE_KEY, selectedMarker?.position?.latitude ?: 0.0)
                    putExtra(Keys.LONGITUDE_KEY, selectedMarker?.position?.longitude ?: 0.0)
                    putExtra(Keys.CITY_KEY, getAddressFromLocation(selectedMarker?.position?.latitude ?: 0.0, selectedMarker?.position?.longitude ?: 0.0))
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } ?: run {
                Toast.makeText(this, getString(R.string.no_location_selected), Toast.LENGTH_SHORT).show()
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    private fun searchLocationByGeocoder(locationName: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(locationName, 1)

        if (addresses!!.isNotEmpty()) {
            val address = addresses[0]
            val latLng = LatLng(address.latitude, address.longitude)
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            addMarker(latLng)
            Toast.makeText(this, "Location found: ${address.locality ?: address.countryName}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.place_not_found, Toast.LENGTH_SHORT).show()
        }
    }




    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationTask: Task<Location> = fusedLocationClient.lastLocation
            locationTask.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val currentLocation = LatLng(latitude, longitude)

                    // Move the camera to the current location and zoom in
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                    addMarker(currentLocation)
                    getAddressFromLocation(latitude, longitude)
                } ?: run {
                    Log.e("MapsActivity", "Location is null")
                }
            }
        }
    }

    private fun addMarker(location: LatLng) {
        selectedMarker?.remove()
        selectedMarker = map?.addMarker(MarkerOptions().position(location))
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        Log.i("DEBUGG", "getAddressFromLocation: $latitude, $longitude")
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        return if (!addresses.isNullOrEmpty()) {
            val city = addresses[0].locality ?:  addresses[0].countryName ?: getString(R.string.unknown)
            Log.i("DEBUGG", "getAddressFromLocation: $city")
            city
        } else {
            Log.e("MapsActivity", "No city found for coordinates: $latitude, $longitude")
            getString(R.string.unknown)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        getLocation()

        map?.setOnMapClickListener { latLng ->
            addMarker(latLng)

            val latitude = latLng.latitude
            val longitude = latLng.longitude
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15f))

            getAddressFromLocation(latitude, longitude)
        }
    }
}
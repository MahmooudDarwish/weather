package com.example.weather.features.home.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.weather.R

class Home : Fragment(), UpdateLocationWeather {

    private lateinit var mapActivityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register the ActivityResultLauncher to receive data from Map activity
        mapActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d("HomeFragment", "Received result with code: ${result.resultCode}")
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val latitude = data.getDoubleExtra("LATITUDE", 0.0)
                    val longitude = data.getDoubleExtra("LONGITUDE", 0.0)
                    Log.d("HomeFragment", "Latitude: $latitude, Longitude: $longitude")
                    if (latitude != 0.0 && longitude != 0.0) {
                    } else {
                        Log.e("HomeFragment", "No location data received")
                    }
                }
            } else {
                Log.e("HomeFragment", "Result code not OK")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_home, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        Log.i("HomeFragment", "onViewCreated called")

    }
    override fun updateLocation(latitude: Double, longitude: Double) {
        Log.d("HomeFragment", "Latitude: $latitude, Longitude: $longitude")
        // Update your UI or ViewModel with the new location data here
    }
}



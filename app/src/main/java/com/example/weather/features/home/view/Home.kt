package com.example.weather.features.home.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.weather.R
import com.example.weather.features.home.view_model.HomeViewModel
import com.example.weather.features.home.view_model.HomeViewModelFactory
import com.example.weather.features.landing.view_model.LandingFactory
import com.example.weather.features.landing.view_model.LandingViewModel
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.model.WeatherRepositoryImpl
import com.example.weather.utils.model.WeatherResponse
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment(), UpdateLocationWeather {
    lateinit var viewModel: HomeViewModel

    private lateinit var countryName: TextView
    private lateinit var todayDate: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var temperatureText: TextView
    private lateinit var weatherDescriptionText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val homeFactory = HomeViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(requireActivity()).weatherDao(),
                    AppDatabase.getDatabase(requireActivity()).forecastDao()
                ),
                sharedPreferences = SharedPreferences(requireActivity())

            )
        )

        viewModel = ViewModelProvider(this, homeFactory).get(HomeViewModel::class.java)

        viewModel.currentWeather.observe(this) { weatherResponse ->
                Log.i("HomeFragment", "Current Weather: $weatherResponse")
                updateUI(weatherResponse)
        }
    }

    private fun updateUI(weatherResponse: WeatherResponse?) {
        countryName.text = weatherResponse?.name
        temperatureText.text = weatherResponse?.main?.temp.toString()
        weatherDescriptionText.text = weatherResponse?.weather?.get(0)?.description
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        super.onViewCreated(view, savedInstanceState)
        Log.i("HomeFragment", "onViewCreated called")
        initUi(view)
        todayDate.text = getCurrentDate()

    }

    private fun initUi(view: View){
        countryName = view.findViewById(R.id.countryName)
        todayDate = view.findViewById(R.id.todayDate)
        weatherIcon = view.findViewById(R.id.weatherIcon)
        temperatureText = view.findViewById(R.id.temperatureText)
        weatherDescriptionText = view.findViewById(R.id.weatherDescriptionText)
    }
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
    override fun updateLocation(latitude: Double, longitude: Double) {
        Log.d("HomeFragment", "Latitude: $latitude, Longitude: $longitude")
        viewModel.getWeather(longitude = longitude, latitude = latitude)
    }
}



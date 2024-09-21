package com.example.weather.features.home.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.features.home.view_model.HomeViewModel
import com.example.weather.features.home.view_model.HomeViewModelFactory
import com.example.weather.features.map.view.Map
import com.example.weather.utils.Utils
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.model.ForecastResponse
import com.example.weather.utils.model.HourlyWeatherResponse
import com.example.weather.utils.model.WeatherRepositoryImpl
import com.example.weather.utils.model.WeatherResponse
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment() , UpdateLocationWeather{
    lateinit var viewModel: HomeViewModel

    private lateinit var countryName: TextView
    private lateinit var todayDate: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var temperatureText: TextView
    private lateinit var weatherDescriptionText: TextView
    private lateinit var locationIcon: ImageView
    private lateinit var recyclerViewHourlyWeather: RecyclerView
    private lateinit var recyclerViewDailyWeather: RecyclerView


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
        viewModel.hourlyWeatherData.observe(this) { hourlyWeather ->
            Log.i("HomeFragment", "Hourly Weather: $hourlyWeather")
            updateHourlyRecyclerView(hourlyWeather)
        }

        viewModel.dailyWeatherData.observe(this) { dailyWeather ->
            Log.i("HomeFragment", "Hourly Weather: $dailyWeather")
            updateDailyRecyclerView(dailyWeather)
        }
    }

    private fun updateDailyRecyclerView(dailyWeather: ForecastResponse?) {
        recyclerViewDailyWeather.adapter = dailyWeather?.let { DailyWeatherAdapter(it) }

    }

    private fun updateHourlyRecyclerView(hourlyWeather: HourlyWeatherResponse?) {
        recyclerViewHourlyWeather.adapter = hourlyWeather?.let { HourlyWeatherAdapter(it) }

    }


    private fun updateUI(weatherResponse: WeatherResponse?) {
        if(weatherResponse?.name!!.isEmpty()){
            countryName.text = getString(R.string.unknown)
        }else{
            countryName.text = weatherResponse.name
        }
        temperatureText.text = weatherResponse.main.temp.toString()
        weatherDescriptionText.text = weatherResponse.weather[0].description
        weatherIcon.setImageResource(Utils().getWeatherIcon(weatherResponse.weather[0].icon))
    }

    override fun onResume() {
        super.onResume()
        Log.i("DEBUGGGGGGG", "onResume called")
        val currentLocation : Pair<Double, Double>? = viewModel.getCurrentLocation()
        Log.i("DEBUGGGGGGG", "currentLocation $currentLocation")
        Log.i("DEBUGGGGGGG", "viewModel.getCurrentLocation() ${viewModel.getLocationStatus()}")
        updateLocation(currentLocation)

        checkLocationStatus()
    }

    private fun checkLocationStatus() {
        if(viewModel.getLocationStatus() == LocationStatus.MAP){
            Log.i("HomeFragment", "LocationStatus is MAP")
            locationIcon.visibility = View.VISIBLE
        }else{
            Log.i("HomeFragment", "LocationStatus is NOT MAP")
            locationIcon.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val currentLocation : Pair<Double, Double>? = viewModel.getCurrentLocation()

        updateLocation(currentLocation)
        Log.i("HomeFragment", "onViewCreated called")
        initUi(view)
        setUpListeners()
        todayDate.text = getCurrentDate()

    }

    private fun setUpListeners() {
        locationIcon.setOnClickListener {
            navigateToMaps()
        }
    }

    private val mapActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra(Keys.LATITUDE_KEY, 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra(Keys.LONGITUDE_KEY, 0.0) ?: 0.0
            Log.d("HomeActivity", "Latitude: $latitude, Longitude: $longitude")
            viewModel.saveCurrentLocation(latitude, longitude)
            updateLocation(Pair(latitude, longitude))
        }
    }

    private fun navigateToMaps() {
        mapActivityResultLauncher.launch(Intent(requireActivity(), Map::class.java))
    }


    private fun initUi(view: View){
        countryName = view.findViewById(R.id.countryName)
        todayDate = view.findViewById(R.id.todayDate)
        weatherIcon = view.findViewById(R.id.weatherIcon)
        temperatureText = view.findViewById(R.id.temperatureText)
        weatherDescriptionText = view.findViewById(R.id.weatherDescriptionText)
        locationIcon = view.findViewById(R.id.locationIcon)
        recyclerViewHourlyWeather = view.findViewById(R.id.recyclerViewHourlyWeather)
        recyclerViewHourlyWeather.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)

        recyclerViewDailyWeather = view.findViewById(R.id.recyclerViewDailyWeather)
        recyclerViewDailyWeather.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        checkLocationStatus()

    }
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
    override fun updateLocation(currentLocation : Pair<Double, Double>?) {
        if (currentLocation == null) {
            return
        }
        val latitude = currentLocation.first
        val longitude = currentLocation.second
        Log.i("HomeFragment", "updateLocation called $latitude $longitude")
        viewModel.getWeather(longitude = longitude, latitude = latitude)
        viewModel.fetchHourlyWeather(longitude = longitude, latitude = latitude)
        viewModel.fetchDailyWeather(longitude = longitude, latitude = latitude)
    }
}


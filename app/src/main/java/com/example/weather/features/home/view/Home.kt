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
import com.example.weather.utils.model.DailyForecastItem
import com.example.weather.utils.model.ForecastItem
import com.example.weather.utils.model.ForecastResponse
import com.example.weather.utils.model.HourlyWeatherResponse
import com.example.weather.utils.model.WeatherRepositoryImpl
import com.example.weather.utils.model.WeatherResponse
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Home : Fragment() , UpdateLocationWeather, OnDayClickListener{
    lateinit var viewModel: HomeViewModel

    private lateinit var countryName: TextView
    private lateinit var todayDate: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var temperatureText: TextView
    private lateinit var weatherDescriptionText: TextView
    private lateinit var locationIcon: ImageView
    private lateinit var recyclerViewHourlyWeather: RecyclerView
    private lateinit var recyclerViewDailyWeather: RecyclerView
    private lateinit var dailyWeatherAdapter: DailyWeatherAdapter
    private lateinit var hourlyWeatherAdapter: HourlyWeatherAdapter

    private lateinit var pressureText: TextView
    private lateinit var humidityText: TextView
    private lateinit var windSpeedText: TextView
    private lateinit var cloudText: TextView

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


    override fun onResume() {
        super.onResume()
        val currentLocation : Pair<Double, Double>? = viewModel.getCurrentLocation()
        updateLocation(currentLocation)
        checkLocationStatus()
    }
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
                updateUI(weatherResponse)
        }
        viewModel.hourlyWeatherData.observe(this) { hourlyWeather ->
            if (hourlyWeather != null) {
                updateHourlyRecyclerViewList(hourlyWeather.list)
            }
        }

        viewModel.dailyWeatherData.observe(this) { dailyWeather ->
            updateDailyRecyclerView(dailyWeather)
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
        todayDate.text = Utils().getCurrentDate()

    }

    private fun initUi(view: View){
        countryName = view.findViewById(R.id.countryName)
        todayDate = view.findViewById(R.id.todayDate)
        weatherIcon = view.findViewById(R.id.weatherIcon)
        temperatureText = view.findViewById(R.id.temperatureText)
        weatherDescriptionText = view.findViewById(R.id.weatherDescriptionText)
        locationIcon = view.findViewById(R.id.locationIcon)
        cloudText = view.findViewById(R.id.cloudText)
        windSpeedText = view.findViewById(R.id.windText)
        humidityText = view.findViewById(R.id.humidityText)
        pressureText = view.findViewById(R.id.pressureText)


        recyclerViewHourlyWeather = view.findViewById(R.id.recyclerViewHourlyWeather)
        recyclerViewHourlyWeather.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        hourlyWeatherAdapter = HourlyWeatherAdapter(emptyList())
        recyclerViewHourlyWeather.adapter = hourlyWeatherAdapter


        recyclerViewDailyWeather = view.findViewById(R.id.recyclerViewDailyWeather)
        recyclerViewDailyWeather.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        dailyWeatherAdapter = DailyWeatherAdapter(emptyList(), this)
        recyclerViewDailyWeather.adapter = dailyWeatherAdapter

        checkLocationStatus()

    }

    private fun setUpListeners() {
        locationIcon.setOnClickListener {
            navigateToMaps()
        }
    }

    private fun updateDailyRecyclerView(dailyWeather: ForecastResponse?) {
        if (dailyWeather != null) {
            dailyWeatherAdapter.updateData(dailyWeather.list)
        }
    }

    private fun updateDetailedWeatherUI(weatherItem: DailyForecastItem) {

        weatherDescriptionText.text = weatherItem.weather[0].description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }

        if(Utils().getDayNameFromEpoch(weatherItem.dt) == Keys.TODAY){
            temperatureText.text = "${viewModel.currentWeather.value?.main?.temp?.toInt()}°C"

        }else{
            temperatureText.text = "${weatherItem.temp.max}/${weatherItem.temp.min.toInt()}°C"
        }
        weatherIcon.setImageResource(Utils().getWeatherIcon(weatherItem.weather[0].icon))

        pressureText.text = getString(R.string.hpa, weatherItem.pressure.toString())
        humidityText.text = getString(R.string.percentage, weatherItem.humidity.toString())
        cloudText.text = getString(R.string.percentage, weatherItem.clouds.toString())
        windSpeedText.text = getString(R.string.m_s, weatherItem.speed.toString())

        val filteredHourlyWeather = filterHourlyWeatherByDay(weatherItem.dt)
        updateHourlyRecyclerViewList(filteredHourlyWeather)

    }

    private fun filterHourlyWeatherByDay(dayEpoch: Long): List<ForecastItem>? {
        return viewModel.hourlyWeatherData.value?.let { hourlyWeather ->
            if (Utils().getDayNameFromEpoch(dayEpoch) == Keys.TODAY) {
                hourlyWeather.list.take(24)
            } else {
                hourlyWeather.list.filter {
                    Utils().isSameDay(it.dt, dayEpoch)
                }
            }
        }
    }

    private fun updateHourlyRecyclerViewList(filteredList : List<ForecastItem>?){
        if (filteredList != null) {
            hourlyWeatherAdapter.updateHourlyWeatherList(filteredList)
        }

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
        pressureText.text = getString(R.string.hpa, weatherResponse.main.pressure.toString())
        humidityText.text = getString(R.string.percentage, weatherResponse.main.humidity.toString())
        windSpeedText.text = getString(R.string.m_s, weatherResponse.wind.speed.toString())
        cloudText.text = getString(R.string.percentage, weatherResponse.clouds.all.toString())

    }

    private fun checkLocationStatus() {
        if(viewModel.getLocationStatus() == LocationStatus.MAP){
            locationIcon.visibility = View.VISIBLE
        }else{
            locationIcon.visibility = View.GONE
        }
    }
    private fun navigateToMaps() {
        mapActivityResultLauncher.launch(Intent(requireActivity(), Map::class.java))
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

    override fun onDayClick(item: DailyForecastItem) {
        updateDetailedWeatherUI(item)
    }
}


package com.example.weather.features.home.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Geocoder
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
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.model.API.DailyForecastItem
import com.example.weather.utils.model.ForecastItem
import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.model.API.WeatherResponse
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl

import java.util.Locale
//UpdateLocationWeather
class Home : Fragment(), OnDayClickListener, UpdateLocationWeather {
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
            updateLocation(Pair(latitude, longitude))
        }
    }


    override fun onResume() {
        super.onResume()
        Log.i("DEBUGGGGGGG", "onResume called Home")
        val currentLocation: Pair<Double, Double>? = viewModel.getCurrentLocation()
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
                    AppDatabase.getDatabase(requireActivity()).alarmDao()
                ),
                sharedPreferences = SharedPreferencesManager(requireActivity().getSharedPreferences(Keys.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE))

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
        val currentLocation: Pair<Double, Double>? = viewModel.getCurrentLocation()

        updateLocation(currentLocation)
        Log.i("HomeFragment", "onViewCreated called")
        initUi(view)
        setUpListeners()
        todayDate.text = Utils().getCurrentDate()

    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireActivity(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        return if (!addresses.isNullOrEmpty()) {
            val city = addresses[0].locality ?: getString(R.string.unknown)
            Log.d("MapsActivity", "City: $city")
            city
        } else {
            Log.e("MapsActivity", "No city found for coordinates: $latitude, $longitude")
            getString(R.string.unknown)
        }
    }

    private fun initUi(view: View) {
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
        recyclerViewHourlyWeather.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        hourlyWeatherAdapter = HourlyWeatherAdapter(emptyList(), viewModel.getWeatherMeasure())
        recyclerViewHourlyWeather.adapter = hourlyWeatherAdapter


        recyclerViewDailyWeather = view.findViewById(R.id.recyclerViewDailyWeather)
        recyclerViewDailyWeather.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        dailyWeatherAdapter =
            DailyWeatherAdapter(emptyList(), this, viewModel.getWeatherMeasure(), requireActivity())
        recyclerViewDailyWeather.adapter = dailyWeatherAdapter

        checkLocationStatus()

    }

    private fun setUpListeners() {
        locationIcon.setOnClickListener {
            navigateToMaps()
        }
    }

    private fun updateDailyRecyclerView(dailyWeather: DailyWeatherResponse?) {
        if (dailyWeather != null) {
            dailyWeatherAdapter.updateData(dailyWeather.list)
        }
    }

    private fun updateDetailedWeatherUI(weatherItem: DailyForecastItem) {

        weatherDescriptionText.text = weatherItem.weather[0].description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }

        val selectedUnit = viewModel.getWeatherMeasure()
        val maxTempInCelsius = weatherItem.temp.max.toInt()
        val minTempInCelsius = weatherItem.temp.min.toInt()
        val convertedMaxTemp = Utils().getWeatherMeasure(maxTempInCelsius, selectedUnit)
        val convertedMinTemp = Utils().getWeatherMeasure(minTempInCelsius, selectedUnit)

        if (Utils().getDayNameFromEpoch(
                context = requireActivity(),
                epochTime = weatherItem.dt
            ) == getString(R.string.today)
        ) {
            temperatureText.text =
                "${convertedMaxTemp.toInt()} ${Utils().getUnitSymbol(selectedUnit)}"
        } else {
            temperatureText.text =
                "${convertedMaxTemp.toInt()}/${convertedMinTemp.toInt()}${
                    Utils().getUnitSymbol(
                        selectedUnit
                    )
                }"
        }

        weatherIcon.setImageResource(Utils().getWeatherIcon(weatherItem.weather[0].icon))

        pressureText.text = "${weatherItem.pressure} ${getString(R.string.hpa)}"
        humidityText.text = getString(R.string.percentage, weatherItem.humidity.toString())
        cloudText.text = getString(R.string.percentage, weatherItem.clouds.toString())

        val speedInMps = weatherItem.speed
        val speedMeasure = viewModel.getWindMeasure()
        val speed = Utils().metersPerSecondToMilesPerHour(speedInMps, speedMeasure)
        windSpeedText.text = getString(
            R.string.wind_speed_format,
            speed,
            Utils().getSpeedUnitSymbol(speedMeasure, requireActivity())
        )

        val filteredHourlyWeather = filterHourlyWeatherByDay(weatherItem.dt)
        updateHourlyRecyclerViewList(filteredHourlyWeather)

    }

    private fun filterHourlyWeatherByDay(dayEpoch: Long): List<ForecastItem>? {
        return viewModel.hourlyWeatherData.value?.let { hourlyWeather ->
            if (Utils().getDayNameFromEpoch(
                    context = requireActivity(),
                    epochTime = dayEpoch
                ) == getString(R.string.today)
            ) {
                hourlyWeather.list.take(24)
            } else {
                hourlyWeather.list.filter {
                    Utils().isSameDay(it.dt, dayEpoch)
                }
            }
        }
    }

    private fun updateHourlyRecyclerViewList(filteredList: List<ForecastItem>?) {
        if (filteredList != null) {
            hourlyWeatherAdapter.updateHourlyWeatherList(filteredList)
        }

    }


    private fun updateUI(weatherResponse: WeatherResponse?) {
        if (weatherResponse?.name!!.isEmpty()) {
            countryName.text =
                getAddressFromLocation(weatherResponse.coord.lat, weatherResponse.coord.lon)
        } else {

            countryName.text = weatherResponse.name
        }

        val temperatureInCelsius = weatherResponse.main.temp.toInt()
        val selectedUnit = viewModel.getWeatherMeasure()
        val convertedTemperature = Utils().getWeatherMeasure(temperatureInCelsius, selectedUnit)

        temperatureText.text = getString(
            R.string.temperature_format, convertedTemperature, Utils().getUnitSymbol(selectedUnit)
        )

        weatherDescriptionText.text = weatherResponse.weather[0].description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        weatherIcon.setImageResource(Utils().getWeatherIcon(weatherResponse.weather[0].icon))
        pressureText.text = "${weatherResponse.main.pressure} ${getString(R.string.hpa)}"
        humidityText.text = getString(R.string.percentage, weatherResponse.main.humidity.toString())
        val speedInMps = weatherResponse.wind.speed
        val speedMeasure = viewModel.getWindMeasure()
        val speed = Utils().metersPerSecondToMilesPerHour(speedInMps, speedMeasure)

        windSpeedText.text = getString(
            R.string.wind_speed_format,
            speed,
            Utils().getSpeedUnitSymbol(speedMeasure, requireActivity())
        )

        cloudText.text = getString(R.string.percentage, weatherResponse.clouds.all.toString())

    }

    private fun checkLocationStatus() {
        if (viewModel.getLocationStatus() == LocationStatus.MAP) {
            locationIcon.visibility = View.VISIBLE
        } else {
            locationIcon.visibility = View.GONE
        }
    }

    private fun navigateToMaps() {
        mapActivityResultLauncher.launch(Intent(requireActivity(), Map::class.java))
    }

    override fun updateLocation(currentLocation: Pair<Double, Double>?) {
        if (currentLocation == null) {
            return
        }
        val latitude = currentLocation.first
        val longitude = currentLocation.second
        viewModel.getWeather(longitude = longitude, latitude = latitude)
        viewModel.fetchHourlyWeather(longitude = longitude, latitude = latitude)
        viewModel.fetchDailyWeather(longitude = longitude, latitude = latitude)
        viewModel.saveCurrentLocation(longitude = longitude, latitude = latitude)

    }

    override fun onDayClick(item: DailyForecastItem) {
        updateDetailedWeatherUI(item)
    }
}


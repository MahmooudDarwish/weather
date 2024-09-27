package com.example.weather.features.weather_deatils.view

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.features.settings.view_model.SettingsViewModel
import com.example.weather.features.settings.view_model.SettingsViewModelFactory
import com.example.weather.features.weather_deatils.view_model.WeatherDetailsViewModel
import com.example.weather.features.weather_deatils.view_model.WeatherDetailsViewModelFactory
import com.example.weather.utils.SharedDataManager
import com.example.weather.utils.Utils
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.enums.Language
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class FavoriteDetails : AppCompatActivity(), OnDayClickedFavorite {

    lateinit var viewModel: WeatherDetailsViewModel

    private lateinit var countryName: TextView
    private lateinit var todayDate: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var temperatureText: TextView
    private lateinit var weatherDescriptionText: TextView
    private lateinit var recyclerViewHourlyWeather: RecyclerView
    private lateinit var recyclerViewDailyWeather: RecyclerView
    private lateinit var dailyWeatherAdapter: FavoriteDailyWeatherAdapter
    private lateinit var hourlyWeatherAdapter: FavoriteHourlyWeatherAdapter
    private lateinit var pressureText: TextView
    private lateinit var humidityText: TextView
    private lateinit var windSpeedText: TextView
    private lateinit var cloudText: TextView
    private lateinit var languageJob: Job

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        Log.i("DEBUGG", "onConfigurationChanged")

        lifecycleScope.launch {
            val language = SharedDataManager.languageFlow.first() // Collect the latest value
            Log.i("DEBUGG", "Latest language: $language")
            when (language) {
                Language.ENGLISH -> updateLocale("en")
                Language.ARABIC -> updateLocale("ar")
            }
        }
        recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_details)

        val latitude = intent.getDoubleExtra(Keys.LATITUDE_KEY, 0.0)
        val longitude = intent.getDoubleExtra(Keys.LONGITUDE_KEY, 0.0)

        val weatherDetailsFactory = WeatherDetailsViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(this).weatherDao(),
                    AppDatabase.getDatabase(this).alarmDao()
                ),
                sharedPreferences =SharedPreferencesManager(this.getSharedPreferences(Keys.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE))

            )
        )
        languageJob = lifecycleScope.launch {
            SharedDataManager.languageFlow.collect { language ->
                Log.i("DEBGUGG", "languageDetails: $language")
                when (language) {
                    Language.ENGLISH -> updateLocale("en")
                    Language.ARABIC -> updateLocale("ar")
                }
            }
        }

        viewModel =
            ViewModelProvider(this, weatherDetailsFactory).get(WeatherDetailsViewModel::class.java)


        initUI()
        viewModel.favoriteWeatherData.observe(this) { weatherResponse ->
            if (weatherResponse != null) {
                Log.d("FavoriteDetails", "Received weather data: $weatherResponse")
                updateUI(weatherResponse)
            }
        }
        viewModel.hourlyWeatherData.observe(this) { hourlyWeather ->
            if (hourlyWeather != null) {
                Log.d("FavoriteDetails", "Received hourly weather data: $hourlyWeather")
                updateHourlyRecyclerViewList(hourlyWeather)
            }
        }
        viewModel.dailyWeatherData.observe(this) { dailyWeather ->
            Log.d("FavoriteDetails", "Received daily weather data: $dailyWeather")
            updateDailyRecyclerView(dailyWeather)
        }

        if (isInternetAvailable()) {
            viewModel.getWeatherAndRefreshRoom(
                latitude,
                longitude,
                getAddressFromLocation(latitude, longitude)

            )
            viewModel.fetchDailyWeather(latitude, longitude)
            viewModel.fetchHourlyWeather(latitude, longitude)
            viewModel.fetchFavoriteWeather(latitude, longitude)
        } else {
            viewModel.fetchFavoriteWeather(latitude, longitude)
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

    private fun updateUI(weatherResponse: WeatherEntity) {
        if (weatherResponse.name.isEmpty()) {
            countryName.text =
                getAddressFromLocation(weatherResponse.latitude, weatherResponse.longitude)
        } else {
            countryName.text = weatherResponse.name
        }

        val temperatureInCelsius = weatherResponse.temp.toInt()
        val selectedUnit = viewModel.getWeatherMeasure()
        val convertedTemperature = Utils().getWeatherMeasure(temperatureInCelsius, selectedUnit)

        temperatureText.text = getString(
            R.string.temperature_format, convertedTemperature, Utils().getUnitSymbol(selectedUnit)
        )

        weatherDescriptionText.text = weatherResponse.description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        weatherIcon.setImageResource(Utils().getWeatherIcon(weatherResponse.icon))
        pressureText.text = getString(
            R.string.pressrue_format,
            weatherResponse.pressure,
            getString(R.string.hpa)
        )
        humidityText.text = getString(R.string.percentage, weatherResponse.humidity)
        val speedInMps = weatherResponse.windSpeed
        val speedMeasure = viewModel.getWindMeasure()
        val speed = Utils().metersPerSecondToMilesPerHour(speedInMps, speedMeasure)

        windSpeedText.text = getString(
            R.string.wind_speed_format,
            speed,
            Utils().getSpeedUnitSymbol(speedMeasure, this)
        )

        cloudText.text = getString(R.string.percentage, weatherResponse.clouds)
    }

    private fun updateDailyRecyclerView(dailyWeather: List<DailyWeatherEntity?>?) {
        if (dailyWeather != null) {
            dailyWeatherAdapter.updateData(dailyWeather)
        }
    }

    private fun updateHourlyRecyclerViewList(filteredList: List<HourlyWeatherEntity?>?) {
        if (filteredList != null) {
            hourlyWeatherAdapter.updateHourlyWeatherList(filteredList)
        }
    }

    private fun updateDailyUI(weatherItem: DailyWeatherEntity) {

        weatherDescriptionText.text = weatherItem.description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }

        val selectedUnit = viewModel.getWeatherMeasure()
        val maxTempInCelsius = weatherItem.maxTemp.toInt()
        val minTempInCelsius = weatherItem.minTemp.toInt()
        val convertedMaxTemp = Utils().getWeatherMeasure(maxTempInCelsius, selectedUnit)
        val convertedMinTemp = Utils().getWeatherMeasure(minTempInCelsius, selectedUnit)

        if (Utils().getDayNameFromEpoch(
                context = this,
                epochTime = weatherItem.dt
            ) == getString(R.string.today)
        ) {
            temperatureText.text = getString(R.string.temperature_format, convertedMaxTemp, Utils().getUnitSymbol(selectedUnit))
        } else {

            val formattedTemp = getString(R.string.temp_min_max_format,
                convertedMaxTemp.toInt(),
                convertedMinTemp.toInt(),
                Utils().getUnitSymbol(selectedUnit))

            temperatureText.text = formattedTemp
        }

        weatherIcon.setImageResource(Utils().getWeatherIcon(weatherItem.icon))

        pressureText.text = getString(R.string.pressrue_format, weatherItem.pressure, getString(R.string.hpa))
        humidityText.text = getString(R.string.percentage, weatherItem.humidity)
        cloudText.text = getString(R.string.percentage, weatherItem.clouds)

        val speedInMps = weatherItem.windSpeed
        val speedMeasure = viewModel.getWindMeasure()
        val speed = Utils().metersPerSecondToMilesPerHour(speedInMps, speedMeasure)
        windSpeedText.text = getString(
            R.string.wind_speed_format,
            speed,
            Utils().getSpeedUnitSymbol(speedMeasure, this))


        val filteredHourlyWeather = filterHourlyWeatherByDay(weatherItem.dt)
        updateHourlyRecyclerViewList(filteredHourlyWeather)

    }

    private fun filterHourlyWeatherByDay(dayEpoch: Long): List<HourlyWeatherEntity?>? {
        return viewModel.hourlyWeatherData.value?.let { hourlyWeather ->
            if (Utils().getDayNameFromEpoch(
                    context = this,
                    epochTime = dayEpoch
                ) == getString(R.string.today)
            ) {
                hourlyWeather.take(24)
            } else {
                hourlyWeather.filter {
                    Utils().isSameDay(it!!.dt, dayEpoch)
                }
            }
        }
    }

    private fun initUI() {
        countryName = findViewById(R.id.favoriteCountryName)
        todayDate = findViewById(R.id.favoriteTodayDate)
        weatherIcon = findViewById(R.id.favoriteWeatherIcon)
        temperatureText = findViewById(R.id.favoriteTemperatureText)
        weatherDescriptionText = findViewById(R.id.favoriteWeatherDescriptionText)
        cloudText = findViewById(R.id.favoriteCloudText)
        windSpeedText = findViewById(R.id.favoriteWindText)
        humidityText = findViewById(R.id.favoriteHumidityText)
        pressureText = findViewById(R.id.favoritePressureText)


        recyclerViewHourlyWeather = findViewById(R.id.favoriteRecyclerViewHourlyWeather)
        recyclerViewHourlyWeather.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hourlyWeatherAdapter =
            FavoriteHourlyWeatherAdapter(emptyList(), viewModel.getWeatherMeasure(), this)
        recyclerViewHourlyWeather.adapter = hourlyWeatherAdapter


        recyclerViewDailyWeather = findViewById(R.id.favoriteRecyclerViewDailyWeather)
        recyclerViewDailyWeather.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dailyWeatherAdapter =
            FavoriteDailyWeatherAdapter(emptyList(), this, viewModel.getWeatherMeasure(), this)
        recyclerViewDailyWeather.adapter = dailyWeatherAdapter


    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
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


    override fun onDayClicked(day: DailyWeatherEntity) {
        updateDailyUI(day)
    }
}

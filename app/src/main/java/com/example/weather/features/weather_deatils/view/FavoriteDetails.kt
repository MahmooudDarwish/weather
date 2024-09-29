package com.example.weather.features.weather_deatils.view

import android.content.res.Configuration
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.databinding.ActivityFavoriteDetailsBinding
import com.example.weather.utils.shared_view_model.WeatherDetailsViewModel
import com.example.weather.utils.shared_view_model.WeatherDetailsViewModelFactory
import com.example.weather.utils.managers.SharedDataManager
import com.example.weather.utils.Utils
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.enums.Language
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.managers.InternetChecker
import com.example.weather.utils.model.DataState
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import com.example.weather.utils.shared_adapters.DailyWeatherAdapter
import com.example.weather.utils.shared_adapters.HourlyWeatherAdapter
import com.example.weather.utils.shared_interfaces.OnDayClickListener
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class FavoriteDetails : AppCompatActivity(), OnDayClickListener {

    private lateinit var viewModel: WeatherDetailsViewModel
    private lateinit var binding: ActivityFavoriteDetailsBinding

    private lateinit var dailyWeatherAdapter: DailyWeatherAdapter
    private lateinit var hourlyWeatherAdapter: HourlyWeatherAdapter
    private lateinit var internetChecker: InternetChecker
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
    override fun onDestroy() {
        super.onDestroy()
        internetChecker.stopMonitoring()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val latitude = intent.getDoubleExtra(Keys.LATITUDE_KEY, 0.0)
        val longitude = intent.getDoubleExtra(Keys.LONGITUDE_KEY, 0.0)
        Log.d("UTR", "AFTER navigate Latitude: $latitude, Longitude: $longitude")

        val weatherDetailsFactory = WeatherDetailsViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(this).weatherDao(),
                    AppDatabase.getDatabase(this).alarmDao()
                ),
                sharedPreferences = SharedPreferencesManager(
                    this.getSharedPreferences(
                        Keys.SHARED_PREFERENCES_NAME,
                        MODE_PRIVATE
                    )
                )

            )
        )
        lifecycleScope.launch {
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
        internetChecker = InternetChecker(this)
        internetChecker.startMonitoring()


        initUI()

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (internetChecker.isInternetAvailable()){
                getWeatherDetails(longitude = longitude, latitude = latitude)
            }else{
                showToast(getString(R.string.no_internet_connection))
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
        setUpCollectors()
        getWeatherDetails(longitude = longitude, latitude = latitude)
    }

    private fun getWeatherDetails(longitude: Double, latitude: Double){
        viewModel.updateWeatherAndRefreshRoom(
            latitude,
            longitude,
            getAddressFromLocation(latitude = latitude, longitude = longitude),
            isFavorite = true
        )
    }
    fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        Log.i("UTR", "getAddressFromLocation latitude:$latitude longitude:$longitude")
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        return if (!addresses.isNullOrEmpty()) {
            val city =
                addresses[0].locality ?: addresses[0].countryName ?: getString(R.string.unknown)
            Log.i("DEBUGG", "City: $city")
            city
        } else {
            Toast.makeText(this, getString(R.string.unknown), Toast.LENGTH_SHORT).show()
            Log.e("MapsActivity", "No city found for coordinates: $latitude, $longitude")
            getString(R.string.unknown)
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    private fun setUpCollectors() {
        lifecycleScope.launch {
            internetChecker.networkStateFlow.collect { isConnected ->
                Log.i("LandingActivity", "Network state changed: $isConnected")
                if (isConnected) {
                    binding.noInternetContainer.visibility = View.GONE
                } else {
                    binding.noInternetContainer.visibility = View.VISIBLE
                }
            }
        }


        lifecycleScope.launch {
            viewModel.weatherState.collect { state ->
                when (state) {
                    is DataState.Loading -> {
                        binding.contentLayout.visibility = View.GONE
                        binding.detailsProgressBar.visibility = View.VISIBLE
                    }

                    is DataState.Success -> {
                        binding.contentLayout.visibility = View.VISIBLE
                        binding.detailsProgressBar.visibility = View.GONE
                        updateUI(state.data)  // Update UI with weather data
                    }

                    is DataState.Error -> {
                        binding.contentLayout.visibility = View.VISIBLE
                        binding.detailsProgressBar.visibility = View.GONE
                        showToast(getString(state.message))
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.hourlyWeatherState.collect { state ->
                when (state) {
                    is DataState.Loading -> {
                    }

                    is DataState.Success -> {
                        updateHourlyRecyclerViewList(state.data)
                    }

                    is DataState.Error -> {
                        showToast(getString(state.message))
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.dailyWeatherState.collect { state ->
                when (state) {
                    is DataState.Loading -> {
                    }

                    is DataState.Success -> {
                        updateDailyRecyclerView(state.data)
                    }

                    is DataState.Error -> {
                        showToast(getString(state.message))
                    }
                }
            }
        }
    }
    private fun updateLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources: Resources = this.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        applicationContext.createConfigurationContext(config)
        applicationContext.resources.updateConfiguration(
            config,
            applicationContext.resources.displayMetrics
        )
    }
    private fun updateUI(weatherResponse: WeatherEntity) {

        binding.favoriteCountryName.text = weatherResponse.name


        val temperatureInCelsius = weatherResponse.temp.toInt()
        val selectedUnit = viewModel.getWeatherMeasure()
        val convertedTemperature = Utils().getWeatherMeasure(temperatureInCelsius, selectedUnit)

        binding.favoriteTemperatureText.text = getString(
            R.string.temperature_format, convertedTemperature, Utils().getUnitSymbol(selectedUnit)
        )

        binding.favoriteWeatherDescriptionText.text = weatherResponse.description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        binding.favoriteWeatherIcon.setImageResource(Utils().getWeatherIcon(weatherResponse.icon))
        binding.favoritePressureText.text = getString(
            R.string.pressrue_format,
            weatherResponse.pressure,
            getString(R.string.hpa)
        )
        binding.favoriteHumidityText.text = getString(R.string.percentage, weatherResponse.humidity)
        val speedInMps = weatherResponse.windSpeed
        val speedMeasure = viewModel.getWindMeasure()
        val speed = Utils().metersPerSecondToMilesPerHour(speedInMps, speedMeasure)

        binding.favoriteWindText.text = getString(
            R.string.wind_speed_format,
            speed,
            Utils().getSpeedUnitSymbol(speedMeasure, this)
        )

        binding.favoriteCloudText.text = getString(R.string.percentage, weatherResponse.clouds)
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

        binding.favoriteWeatherDescriptionText.text = weatherItem.description.replaceFirstChar {
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
            binding.favoriteTemperatureText.text = getString(
                R.string.temperature_format,
                convertedMaxTemp,
                Utils().getUnitSymbol(selectedUnit)
            )
        } else {

            val formattedTemp = getString(
                R.string.temp_min_max_format,
                convertedMaxTemp.toInt(),
                convertedMinTemp.toInt(),
                Utils().getUnitSymbol(selectedUnit)
            )

            binding.favoriteTemperatureText.text = formattedTemp
        }

        binding.favoriteWeatherIcon.setImageResource(Utils().getWeatherIcon(weatherItem.icon))

        binding.favoritePressureText.text =
            getString(R.string.pressrue_format, weatherItem.pressure, getString(R.string.hpa))
        binding.favoriteHumidityText.text = getString(R.string.percentage, weatherItem.humidity)
        binding.favoriteCloudText.text = getString(R.string.percentage, weatherItem.clouds)

        val speedInMps = weatherItem.windSpeed
        val speedMeasure = viewModel.getWindMeasure()
        val speed = Utils().metersPerSecondToMilesPerHour(speedInMps, speedMeasure)
        binding.favoriteWindText.text = getString(
            R.string.wind_speed_format,
            speed,
            Utils().getSpeedUnitSymbol(speedMeasure, this)
        )


        filterHourlyWeatherByDay(weatherItem.dt)

    }
    private fun filterHourlyWeatherByDay(dayEpoch: Long) {
        viewModel.hourlyWeatherState.value.let { hourlyWeather ->

            val list = (hourlyWeather as DataState.Success).data
            if (Utils().getDayNameFromEpoch(
                    context = this,
                    epochTime = dayEpoch
                ) == getString(R.string.today)
            ) {
                val filteredList = list.take(24)
                updateHourlyRecyclerViewList(filteredList)

            } else {
                val filteredList = list.filter {
                    Utils().isSameDay(it!!.dt, dayEpoch)
                }
                updateHourlyRecyclerViewList(filteredList)

            }

        }
    }
    private fun initUI() {
        binding.favoriteRecyclerViewHourlyWeather.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hourlyWeatherAdapter =
           HourlyWeatherAdapter(emptyList(), viewModel.getWeatherMeasure(), this)
        binding.favoriteRecyclerViewHourlyWeather.adapter = hourlyWeatherAdapter

        binding.favoriteRecyclerViewDailyWeather.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dailyWeatherAdapter =
            DailyWeatherAdapter(emptyList(), this, viewModel.getWeatherMeasure(), this)
        binding.favoriteRecyclerViewDailyWeather.adapter = dailyWeatherAdapter
    }
    override fun onDayClick(item: DailyWeatherEntity) {
        updateDailyUI(item)
    }
}

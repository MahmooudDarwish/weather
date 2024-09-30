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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.databinding.FragmentHomeBinding
import com.example.weather.features.map.view.Map
import com.example.weather.utils.Utils
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.managers.GPSChecker
import com.example.weather.utils.managers.InternetChecker
import com.example.weather.utils.model.DataState
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import com.example.weather.utils.shared_adapters.DailyWeatherAdapter
import com.example.weather.utils.shared_adapters.HourlyWeatherAdapter
import com.example.weather.utils.shared_interfaces.OnDayClickListener
import com.example.weather.utils.shared_view_model.WeatherDetailsViewModel
import com.example.weather.utils.shared_view_model.WeatherDetailsViewModelFactory
import kotlinx.coroutines.launch

import java.util.Locale

class Home : Fragment(), OnDayClickListener {
    lateinit var viewModel: WeatherDetailsViewModel

    private lateinit var binding: FragmentHomeBinding

    private lateinit var hourlyWeatherAdapter: HourlyWeatherAdapter
    private lateinit var dailyWeatherAdapter: DailyWeatherAdapter

    private var lastKnownLocation: Pair<Double, Double>? = null

    private lateinit var internetChecker: InternetChecker
    private lateinit var gpsChecker: GPSChecker

    private val mapActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra(Keys.LATITUDE_KEY, 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra(Keys.LONGITUDE_KEY, 0.0) ?: 0.0

            Log.d("HomeActivity", "LatitudeMap: $latitude, LongitudeMAp: $longitude")
            viewModel.saveCurrentLocation(latitude, longitude)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        internetChecker.stopMonitoring()
        gpsChecker.stopMonitoring()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val weatherDetailsFactory = WeatherDetailsViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(requireActivity()).weatherDao(),
                    AppDatabase.getDatabase(requireActivity()).alarmDao()
                ),
                sharedPreferences = SharedPreferencesManager(
                    requireActivity().getSharedPreferences(
                        Keys.SHARED_PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                    )
                )

            ),
        )
        viewModel = ViewModelProvider(this, weatherDetailsFactory).get(WeatherDetailsViewModel::class.java)

        internetChecker = InternetChecker(requireActivity())
        gpsChecker = GPSChecker(requireActivity())


        internetChecker.startMonitoring()
        gpsChecker.startMonitoring()



    }

    private fun showToast(msg: String) {
        Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun setUpCollectors() {
        lifecycleScope.launch {
            internetChecker.networkStateFlow.collect { isConnected ->
                if (isConnected) {
                    updateLocation(lastKnownLocation)
                }
            }
        }


        lifecycleScope.launch {
            viewModel.currentLocationFlow.collect { location ->
                Log.i("DEBUGGGGGGG", "WeatherDetailsViewModel $location")
                if (location != null) {
                    lastKnownLocation = location
                    updateLocation(location)
                }
            }

        }
        lifecycleScope.launch {
            viewModel.weatherState.collect { state ->
                when (state) {
                    is DataState.Loading -> {
                        binding.homeContent.visibility = View.GONE
                        binding.contentProgressBar.visibility = View.VISIBLE
                    }
                    is DataState.Success -> {
                        binding.homeContent.visibility = View.VISIBLE
                        binding.contentProgressBar.visibility = View.GONE
                        updateUI(state.data)  // Update UI with weather data
                    }
                    is DataState.Error -> {
                        binding.homeContent.visibility = View.VISIBLE
                        binding.contentProgressBar.visibility = View.GONE
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        Log.i("HomeFragment", "onViewCreated called")
        initUi()
        setUpListeners()
        setUpCollectors()

        binding.todayDate.text = Utils().getCurrentDate()

    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireActivity(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        return if (!addresses.isNullOrEmpty()) {
            val city =
                addresses[0].locality ?: addresses[0].countryName ?: getString(R.string.unknown)
            Log.d("MapsActivity", "City: $city")
            city
        } else {
            Log.e("MapsActivity", "No city found for coordinates: $latitude, $longitude")
            getString(R.string.unknown)
        }
    }


    private fun initUi() {
        binding.recyclerViewHourlyWeather.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        hourlyWeatherAdapter =
            HourlyWeatherAdapter(emptyList(), viewModel.getWeatherMeasure(), requireActivity())
        binding.recyclerViewHourlyWeather.adapter = hourlyWeatherAdapter
        binding.recyclerViewDailyWeather.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        dailyWeatherAdapter =
            DailyWeatherAdapter(emptyList(), this, viewModel.getWeatherMeasure(), requireActivity())
        binding.recyclerViewDailyWeather.adapter = dailyWeatherAdapter
        checkLocationStatus()


        binding.swipeRefreshLayout.setOnRefreshListener {
            if (!internetChecker.isInternetAvailable()) {
                showToast(getString(R.string.no_internet_connection))
                viewModel.fetchWeatherFromRoom(latitude = lastKnownLocation!!.first, longitude = lastKnownLocation!!.second)

            } else {
                if (viewModel.getLocationStatus() == LocationStatus.GPS) {
                    if(gpsChecker.isLocationEnabled()){
                        updateLocation(lastKnownLocation)
                    }else{
                        showToast(getString(R.string.gps_disabled_message))
                    }
                } else {
                        updateLocation(lastKnownLocation)

                }
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }

        updateLocation(lastKnownLocation)


    }

    private fun setUpListeners() {
        binding.locationIcon.setOnClickListener {
            if(InternetChecker(requireActivity()).isInternetAvailable()){
                navigateToMaps()
            }else{
                Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateDailyRecyclerView(dailyList: List<DailyWeatherEntity?>) {
        dailyWeatherAdapter.updateData(dailyList)
    }

    private fun updateDetailedWeatherUI(weatherItem: DailyWeatherEntity) {

        binding.weatherDescriptionText.text = weatherItem.description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }

        val selectedUnit = viewModel.getWeatherMeasure()
        val maxTempInCelsius = weatherItem.maxTemp.toInt()
        val minTempInCelsius = weatherItem.minTemp.toInt()
        val convertedMaxTemp = Utils().getWeatherMeasure(maxTempInCelsius, selectedUnit)
        val convertedMinTemp = Utils().getWeatherMeasure(minTempInCelsius, selectedUnit)

        if (Utils().getDayNameFromEpoch(
                context = requireActivity(),
                epochTime = weatherItem.dt
            ) == getString(R.string.today)
        ) {
            binding.temperatureText.text = getString(
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

            binding.temperatureText.text = formattedTemp

        }

        binding.weatherIcon.setImageResource(Utils().getWeatherIcon(weatherItem.icon))

        binding.pressureText.text =
            getString(R.string.pressrue_format, weatherItem.pressure, getString(R.string.hpa))
        binding.humidityText.text = getString(R.string.percentage, weatherItem.humidity)
        binding.cloudText.text = getString(R.string.percentage, weatherItem.clouds)
        val speedInMps = weatherItem.windSpeed
        val speedMeasure = viewModel.getWindMeasure()
        val speed = Utils().metersPerSecondToMilesPerHour(speedInMps, speedMeasure)
        binding.windText.text = getString(
            R.string.wind_speed_format,
            speed,
            Utils().getSpeedUnitSymbol(speedMeasure, requireActivity())
        )

        filterHourlyWeatherByDay(weatherItem.dt)

    }



    private fun filterHourlyWeatherByDay(dayEpoch: Long) {
        viewModel.hourlyWeatherState.value.let { hourlyWeather ->

            val list = (hourlyWeather as DataState.Success).data
            if (Utils().getDayNameFromEpoch(
                    context = requireActivity(),
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

    private fun updateHourlyRecyclerViewList(filteredList: List<HourlyWeatherEntity?>) {
        Log.i("HomeFragment", "updateHourlyRecyclerViewList called")
        Log.i("HomeFragment", "updateHourlyRecyclerViewList ${filteredList.size}")
        hourlyWeatherAdapter.updateHourlyWeatherList(filteredList)
    }

    private fun updateUI(weatherResponse: WeatherEntity?) {
        if (weatherResponse == null) {
            return
        }
        if (weatherResponse.name.isEmpty()) {
            binding.countryName.text =
                getAddressFromLocation(weatherResponse.latitude, weatherResponse.longitude)
        } else {
            binding.countryName.text = weatherResponse.name
        }

        val temperatureInCelsius = weatherResponse.temp.toInt()
        val selectedUnit = viewModel.getWeatherMeasure()
        val convertedTemperature = Utils().getWeatherMeasure(temperatureInCelsius, selectedUnit)

        binding.temperatureText.text = getString(
            R.string.temperature_format, convertedTemperature, Utils().getUnitSymbol(selectedUnit)
        )

        binding.weatherDescriptionText.text = weatherResponse.description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        binding.weatherIcon.setImageResource(Utils().getWeatherIcon(weatherResponse.icon))
        binding.pressureText.text = getString(
            R.string.pressrue_format,
            weatherResponse.pressure,
            getString(R.string.hpa)
        )
        binding.humidityText.text = getString(R.string.percentage, weatherResponse.humidity)
        val speedInMps = weatherResponse.windSpeed
        val speedMeasure = viewModel.getWindMeasure()
        val speed = Utils().metersPerSecondToMilesPerHour(speedInMps, speedMeasure)

        binding.windText.text = getString(
            R.string.wind_speed_format,
            speed,
            Utils().getSpeedUnitSymbol(speedMeasure, requireActivity())
        )

        binding.cloudText.text = getString(R.string.percentage, weatherResponse.clouds)

    }

    private fun checkLocationStatus() {
        if (viewModel.getLocationStatus() == LocationStatus.MAP) {
            binding.locationIcon.visibility = View.VISIBLE
        } else {
            binding.locationIcon.visibility = View.GONE
        }
    }

    private fun navigateToMaps() {
        mapActivityResultLauncher.launch(Intent(requireActivity(), Map::class.java))
    }

     private fun updateLocation(currentLocation: Pair<Double, Double>?) {
        if (currentLocation == null) {
            return
        }
         checkLocationStatus()
         val latitude = currentLocation.first
        val longitude = currentLocation.second
         if (internetChecker.isInternetAvailable()){
             viewModel.updateWeatherAndRefreshRoom(
                 longitude = longitude,
                 latitude = latitude,
                 city = getAddressFromLocation(latitude, longitude),
                 isFavorite = false
             )
         }else{
             Log.i("HomeFragment", "updateLocation called")
             viewModel.fetchWeatherFromRoom(latitude = latitude,longitude=  longitude)
         }

        Log.i("HomeFragment", " hommmme shared ${longitude}, ")

    }

    override fun onDayClick(item: DailyWeatherEntity) {
        updateDetailedWeatherUI(item)
    }
}


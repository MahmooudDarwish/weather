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
import com.example.weather.features.home.view_model.HomeViewModel
import com.example.weather.features.home.view_model.HomeViewModelFactory
import com.example.weather.features.map.view.Map
import com.example.weather.utils.Utils
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.model.API.ApiResponse
import com.example.weather.utils.model.API.DailyForecastItem
import com.example.weather.utils.model.ForecastItem
import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.model.API.WeatherResponse
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.launch

import java.util.Locale
//UpdateLocationWeather
class Home : Fragment(), OnDayClickListener, UpdateLocationWeather {
    lateinit var viewModel: HomeViewModel

    private lateinit var binding: FragmentHomeBinding

    private lateinit var hourlyWeatherAdapter: HourlyWeatherAdapter
    private lateinit var dailyWeatherAdapter: DailyWeatherAdapter

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
    }

    private fun setUpCollectors() {
        lifecycleScope.launch{
            viewModel.currentWeatherState.collect { apiResponse ->
                when (apiResponse) {
                    is ApiResponse.Loading -> {
                        binding.currentWeatherProgressBar.visibility = View.VISIBLE
                        binding.measurementsProgressBar.visibility = View.VISIBLE
                        binding.measurementsGrid.visibility = View.GONE

                    }
                    is ApiResponse.Success -> {
                        binding.currentWeatherProgressBar.visibility = View.GONE
                        binding.measurementsProgressBar.visibility = View.GONE
                        binding.measurementsGrid.visibility = View.VISIBLE
                        updateUI(apiResponse.data)
                    }
                    is ApiResponse.Error -> {
                        Toast.makeText(requireContext(), getString(apiResponse.message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch{
            viewModel.hourlyWeatherState.collect { apiResponse ->
                when (apiResponse) {
                    is ApiResponse.Loading -> {

                        binding.hourlyWeatherProgressBar.visibility = View.VISIBLE

                    }
                    is ApiResponse.Success -> {
                        binding.hourlyWeatherProgressBar.visibility = View.GONE
                        updateHourlyRecyclerViewList(apiResponse.data?.list)
                    }

                    is ApiResponse.Error -> {
                        Toast.makeText(
                            requireContext(),
                            getString(apiResponse.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        lifecycleScope.launch {
            viewModel.dailyWeatherState.collect { apiResponse ->
                when (apiResponse) {
                    is ApiResponse.Loading -> {
                        binding.dailyWeatherProgressBar.visibility = View.VISIBLE

                    }

                    is ApiResponse.Success -> {
                        binding.dailyWeatherProgressBar.visibility = View.GONE
                        updateDailyRecyclerView(apiResponse.data)
                    }

                    is ApiResponse.Error -> {
                        Toast.makeText(
                            requireContext(),
                            getString(apiResponse.message),
                            Toast.LENGTH_SHORT
                        ).show()
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
        val currentLocation: Pair<Double, Double>? = viewModel.getCurrentLocation()

        updateLocation(currentLocation)
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
            val city = addresses[0].locality ?: addresses[1].locality ?: addresses[0].countryName ?:getString(R.string.unknown)
            Log.d("MapsActivity", "City: $city")
            city
        } else {
            Log.e("MapsActivity", "No city found for coordinates: $latitude, $longitude")
            getString(R.string.unknown)
        }
    }



    private fun initUi() {
        binding.recyclerViewHourlyWeather.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        hourlyWeatherAdapter = HourlyWeatherAdapter(emptyList(), viewModel.getWeatherMeasure(), requireActivity())
        binding.recyclerViewHourlyWeather.adapter = hourlyWeatherAdapter
        binding.recyclerViewDailyWeather.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        dailyWeatherAdapter =
            DailyWeatherAdapter(emptyList(), this, viewModel.getWeatherMeasure(), requireActivity())
        binding.recyclerViewDailyWeather.adapter = dailyWeatherAdapter
        checkLocationStatus()

    }

    private fun setUpListeners() {
        binding.locationIcon.setOnClickListener {
            navigateToMaps()
        }
    }

    private fun updateDailyRecyclerView(dailyWeather: DailyWeatherResponse?) {
        if (dailyWeather != null) {
            dailyWeatherAdapter.updateData(dailyWeather.list)
        }
    }

    private fun updateDetailedWeatherUI(weatherItem: DailyForecastItem) {

        binding.weatherDescriptionText.text = weatherItem.weather[0].description.replaceFirstChar {
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
            binding.temperatureText.text = getString(R.string.temperature_format, convertedMaxTemp, Utils().getUnitSymbol(selectedUnit))
        } else {

            val formattedTemp = getString(R.string.temp_min_max_format,
                convertedMaxTemp.toInt(),
                convertedMinTemp.toInt(),
                Utils().getUnitSymbol(selectedUnit))

            binding.temperatureText.text = formattedTemp

        }

        binding.weatherIcon.setImageResource(Utils().getWeatherIcon(weatherItem.weather[0].icon))

        binding.pressureText.text =
            getString(R.string.pressrue_format, weatherItem.pressure, getString(R.string.hpa))
        binding.humidityText.text = getString(R.string.percentage, weatherItem.humidity)
        binding.cloudText.text = getString(R.string.percentage, weatherItem.clouds)
        val speedInMps = weatherItem.speed
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hourlyWeatherState.collect { apiResponse ->
                when (apiResponse) {
                    is ApiResponse.Loading -> {
                    }
                    is ApiResponse.Success -> {
                        val hourlyWeather = apiResponse.data
                        val filteredList = hourlyWeather?.let { hourly ->
                            if (Utils().getDayNameFromEpoch(
                                    context = requireActivity(),
                                    epochTime = dayEpoch
                                ) == getString(R.string.today)
                            ) {
                                hourly.list.take(24)
                            } else {
                                hourly.list.filter {
                                    Utils().isSameDay(it.dt, dayEpoch)
                                }
                            }
                        }
                        updateHourlyRecyclerViewList(filteredList)
                    }
                    is ApiResponse.Error -> {
                    }
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
            binding.countryName.text =
                getAddressFromLocation(weatherResponse.coord.lat, weatherResponse.coord.lon)
        } else {
            binding.countryName.text = weatherResponse.name
        }

        val temperatureInCelsius = weatherResponse.main.temp.toInt()
        val selectedUnit = viewModel.getWeatherMeasure()
        val convertedTemperature = Utils().getWeatherMeasure(temperatureInCelsius, selectedUnit)

        binding.temperatureText.text = getString(
            R.string.temperature_format, convertedTemperature, Utils().getUnitSymbol(selectedUnit)
        )

        binding.weatherDescriptionText.text = weatherResponse.weather[0].description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        binding.weatherIcon.setImageResource(Utils().getWeatherIcon(weatherResponse.weather[0].icon))
        binding.pressureText.text = getString(
            R.string.pressrue_format,
            weatherResponse.main.pressure,
            getString(R.string.hpa)
        )
        binding.humidityText.text = getString(R.string.percentage, weatherResponse.main.humidity)
        val speedInMps = weatherResponse.wind.speed
        val speedMeasure = viewModel.getWindMeasure()
        val speed = Utils().metersPerSecondToMilesPerHour(speedInMps, speedMeasure)

        binding.windText.text = getString(
            R.string.wind_speed_format,
            speed,
            Utils().getSpeedUnitSymbol(speedMeasure, requireActivity())
        )

        binding.cloudText.text = getString(R.string.percentage, weatherResponse.clouds.all)

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


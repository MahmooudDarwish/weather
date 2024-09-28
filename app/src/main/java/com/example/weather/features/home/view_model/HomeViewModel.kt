package com.example.weather.features.home.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.utils.managers.SharedDataManager
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.model.API.toDailyWeatherEntities
import com.example.weather.utils.model.API.toHourlyWeatherEntities
import com.example.weather.utils.model.API.toWeatherEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException

class HomeViewModel(
    private val weatherRepository: WeatherRepositoryImpl,

) : ViewModel() {

    val currentLocationFlow: SharedFlow<Pair<Double, Double>?> = SharedDataManager.currentLocationFlow

    private val _hourlyWeatherData = MutableStateFlow<List<HourlyWeatherEntity?>>(emptyList())
    val hourlyWeatherData: StateFlow<List<HourlyWeatherEntity?>>
        get() = _hourlyWeatherData

    private val _dailyWeatherData =
        MutableStateFlow<List<DailyWeatherEntity?>>(emptyList())
    val dailyWeatherData: StateFlow<List<DailyWeatherEntity?>>
        get() = _dailyWeatherData


    private val _currentWeatherData =
        MutableStateFlow<WeatherEntity?>(null)
    val currentWeatherData: StateFlow<WeatherEntity?>
        get() = _currentWeatherData

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        val errorMessage = when (exception) {
            is IOException -> R.string.network_error
            is HttpException -> R.string.server_error
            is TimeoutException -> R.string.timeout_error
            else -> R.string.unexpected_error
        }
        _errorState.value = errorMessage
        Log.e("WeatherDetailsViewModel", "Caught an exception: $exception")
    }

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean>
        get() = _loadingState

    private val _errorState = MutableStateFlow(0)
    val errorState: StateFlow<Int>
        get() = _errorState

    private val supervisorJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(Dispatchers.IO + supervisorJob + exceptionHandler)

    fun updateWeatherAndRefreshRoom(latitude: Double, longitude: Double, city: String) {
        _loadingState.value = true
        Log.d("HomeViewModel", "updateWeatherAndRefreshRoom called")
        viewModelScope.launch {
            try {

                Log.d("WeatherDetailsViewModel", "$latitude $longitude")
                // Collect and save current weather data
                weatherRepository.fetchWeatherData(longitude, latitude)
                    .map { response -> response?.toWeatherEntity(city,lon = longitude.toString(), lat =  latitude.toString(), false, ) }
                    .collect { currentWeatherEntity ->
                        Log.d("HomeViewModel", "Current weather entity: $currentWeatherEntity")
                        currentWeatherEntity?.let {
                            weatherRepository.insertWeather(it)
                        }
                    }

                // Fetch and save daily weather
                weatherRepository.get5DayForecast(longitude, latitude)
                    .map { response -> response?.toDailyWeatherEntities(lon = longitude.toString(), lat =latitude.toString(), false,) ?: emptyList() }
                    .collect { dailyWeatherEntities ->
                        weatherRepository.insertDailyWeather(dailyWeatherEntities)
                    }

                // Fetch and save hourly weather
                weatherRepository.fetchHourlyWeatherData(longitude, latitude)
                    .map { response -> response?.toHourlyWeatherEntities(lon = longitude.toString(),lat= latitude.toString(), false,) ?: emptyList() }
                    .collect { hourlyWeatherEntities ->
                        weatherRepository.insertHourlyWeather(hourlyWeatherEntities)
                    }

                fetchWeatherFromRoom(longitude = longitude, latitude =  latitude)
            } catch (e: Exception) {
                _errorState.value = R.string.error_fetching_favorite_weather_data
            } finally {
                _loadingState.value = false

            }
        }
    }

    private fun fetchWeatherFromRoom(latitude : Double, longitude : Double) {
        viewModelScope.launch {
            try {
                launch {
                    weatherRepository.getHourlyWeather(lat = latitude, lon =longitude)
                        .collect { response ->
                            Log.d("HomeViewModel", "toWeather $latitude $longitude")
                            Log.d("WeatherDetailsViewModel", "Hourly weather data: $response")
                            _hourlyWeatherData.value = response
                        }
                }
                launch {
                    weatherRepository.getDailyWeather(lon = longitude, lat = latitude)
                        .collect { response ->
                            _dailyWeatherData.value = response
                        }
                }
                launch {
                    weatherRepository.getWeather(lon = longitude, lat = latitude)
                        .collect { response ->
                            _currentWeatherData.value = response
                        }
                }
            } catch (e: Exception) {
                _errorState.value = R.string.error_fetching_favorite_weather_data
                Log.e("WeatherDetailsViewModel", "Error fetching favorite weather data", e)
            }
        }
    }

    fun getWeatherMeasure(): Temperature {
        return weatherRepository.getTemperatureUnit()
    }

    fun getWindMeasure(): WindSpeed {
        return weatherRepository.getWindSpeedUnit()
    }

    fun getLocationStatus(): LocationStatus {
        return weatherRepository.getLocationStatus()
    }
    fun saveCurrentLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val location = Pair(latitude, longitude)
            SharedDataManager.emitLocation(location)
            weatherRepository.saveCurrentLocation(latitude, longitude)
        }
    }

}


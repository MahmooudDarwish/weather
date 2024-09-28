package com.example.weather.features.weather_deatils.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.weather.R
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed

import com.example.weather.utils.model.API.toDailyWeatherEntities
import com.example.weather.utils.model.API.toHourlyWeatherEntities
import com.example.weather.utils.model.API.toWeatherEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException

class WeatherDetailsViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {

    private val _hourlyWeatherData = MutableStateFlow<List<HourlyWeatherEntity?>>(emptyList())
    val hourlyWeatherData: StateFlow<List<HourlyWeatherEntity?>>
        get() = _hourlyWeatherData

    private val _dailyWeatherData =
        MutableStateFlow<List<DailyWeatherEntity?>>(emptyList())
    val dailyWeatherData: StateFlow<List<DailyWeatherEntity?>>
        get() = _dailyWeatherData


    private val _favoriteWeatherData =
        MutableStateFlow<WeatherEntity?>(null)
    val favoriteWeatherData: StateFlow<WeatherEntity?>
        get() = _favoriteWeatherData



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

        Log.d("WeatherDetailsViewModel", "Updating weather and refreshing Room data")
        viewModelScope.launch {
            try {
                // Collect and save current weather data
                weatherRepository.fetchWeatherData(longitude, latitude)
                    .map { response -> response?.toWeatherEntity(city, lon = longitude.toString(),lat = latitude.toString() , isFavorite = true) }
                    .collect { currentWeatherEntity ->
                        currentWeatherEntity?.let {
                            weatherRepository.insertWeather(it)
                        }
                    }

                // Fetch and save daily weather
                weatherRepository.get5DayForecast(longitude, latitude)
                    .map { response -> response?.toDailyWeatherEntities(longitude.toString(), latitude.toString(), true) ?: emptyList() }
                    .collect { dailyWeatherEntities ->
                        weatherRepository.insertDailyWeather(dailyWeatherEntities)
                    }

                // Fetch and save hourly weather
                weatherRepository.fetchHourlyWeatherData(longitude, latitude)
                    .map { response -> response?.toHourlyWeatherEntities(lon = longitude.toString(), lat = latitude.toString() , isFavorite = true) ?: emptyList() }
                    .collect { hourlyWeatherEntities ->
                        weatherRepository.insertHourlyWeather(hourlyWeatherEntities)
                    }

                fetchFavoriteWeather(latitude, longitude)
            } catch (e: Exception) {
                _errorState.value = R.string.error_fetching_favorite_weather_data
                Log.e("WeatherDetailsViewModel", "Error fetching or saving weather data", e)
            } finally {
                _loadingState.value = false

            }
        }
    }

    private fun fetchFavoriteWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            Log.d("lllllllllll", "Fetching favorite weather data")
            try {
                launch {
                    weatherRepository.getHourlyWeather(longitude, latitude)
                        .collect { response ->
                            _hourlyWeatherData.value = response
                        }
                }
                launch {
                    weatherRepository.getDailyWeather(longitude, latitude)
                        .collect { response ->
                            _dailyWeatherData.value = response
                        }
                }
                launch {
                    weatherRepository.getWeather(longitude, latitude)
                        .collect { response ->
                            _favoriteWeatherData.value = response
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
    override fun onCleared() {
        super.onCleared()
        supervisorJob.cancel()
    }
}

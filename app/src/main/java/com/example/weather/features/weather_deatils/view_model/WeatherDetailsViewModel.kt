package com.example.weather.features.weather_deatils.view_model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.model.API.ApiResponse

import com.example.weather.utils.model.API.WeatherResponse
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
        Log.e("WeatherDetailsViewModel", "Caught an exception: $exception")
    }

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean>
        get() = _loadingState

    private val supervisorJob = SupervisorJob()

    private val viewModelScope = CoroutineScope(Dispatchers.IO + supervisorJob + exceptionHandler)

    fun updateWeatherAndRefreshRoom(latitude: Double, longitude: Double, city: String) {
        _loadingState.value = true

        viewModelScope.launch {
            try {
                // Collect and save current weather data
                weatherRepository.fetchWeatherData(longitude, latitude)
                    .map { response -> response?.toWeatherEntity(city) }
                    .collect { currentWeatherEntity ->
                        Log.d("WeatherDetailsViewModel", "Current Weather Entity: $currentWeatherEntity")
                        currentWeatherEntity?.let {
                            weatherRepository.insertFavoriteWeather(it)
                        }
                    }

                // Fetch and save daily weather
                weatherRepository.get5DayForecast(longitude, latitude)
                    .map { response -> response?.toDailyWeatherEntities() ?: emptyList() }
                    .collect { dailyWeatherEntities ->
                        Log.d("WeatherDetailsViewModel", "Daily Weather Entities: $dailyWeatherEntities")
                        weatherRepository.insertFavoriteDailyWeather(dailyWeatherEntities)
                    }

                // Fetch and save hourly weather
                weatherRepository.fetchHourlyWeatherData(longitude, latitude)
                    .map { response -> response?.toHourlyWeatherEntities() ?: emptyList() }
                    .collect { hourlyWeatherEntities ->
                        weatherRepository.insertFavoriteHourlyWeather(hourlyWeatherEntities)
                    }

                fetchFavoriteWeather(latitude, longitude)
            } catch (e: Exception) {
                Log.e("WeatherDetailsViewModel", "Error fetching or saving weather data", e)
            } finally {
                _loadingState.value = false

            }
        }
    }

    fun fetchFavoriteWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                launch {
                    weatherRepository.getFavoriteHourlyWeather(longitude, latitude)
                        .collect { response ->
                            _hourlyWeatherData.value = response
                        }
                }
                launch {
                    weatherRepository.getFavoriteDailyWeather(longitude, latitude)
                        .collect { response ->
                            _dailyWeatherData.value = response
                        }
                }
                launch {
                    weatherRepository.getFavoriteWeather(longitude, latitude)
                        .collect { response ->
                            _favoriteWeatherData.value = response
                        }
                }
            } catch (e: Exception) {
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

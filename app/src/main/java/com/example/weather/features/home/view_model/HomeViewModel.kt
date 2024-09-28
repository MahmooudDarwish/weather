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
import com.example.weather.utils.model.DataState
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

    private val _weatherState = MutableStateFlow<DataState<WeatherEntity>>(DataState.Loading)
    val weatherState: StateFlow<DataState<WeatherEntity>>
        get() = _weatherState

    private val _hourlyWeatherState = MutableStateFlow<DataState<List<HourlyWeatherEntity?>>>(DataState.Loading)
    val hourlyWeatherState: StateFlow<DataState<List<HourlyWeatherEntity?>>>
        get() = _hourlyWeatherState

    private val _dailyWeatherState = MutableStateFlow<DataState<List<DailyWeatherEntity?>>>(DataState.Loading)
    val dailyWeatherState: StateFlow<DataState<List<DailyWeatherEntity?>>>
        get() = _dailyWeatherState

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        val errorMessage = when (exception) {
            is IOException -> R.string.network_error
            is HttpException -> R.string.server_error
            is TimeoutException -> R.string.timeout_error
            else -> R.string.unexpected_error
        }
        // Emit an error state
        _weatherState.value = DataState.Error(errorMessage)
        Log.e("HomeViewModel", "Caught an exception: $exception")
    }

    private val viewModelScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)

    fun updateWeatherAndRefreshRoom(latitude: Double, longitude: Double, city: String) {
        _weatherState.value = DataState.Loading
        viewModelScope.launch {
            try {
                // Fetch and save current weather data
                weatherRepository.fetchWeatherData(longitude, latitude)
                    .map { response -> response?.toWeatherEntity(city, lon = longitude.toString(), lat = latitude.toString(), false) }
                    .collect { currentWeatherEntity ->
                        currentWeatherEntity?.let {
                            weatherRepository.insertWeather(it)
                            _weatherState.value = DataState.Success(it)
                        }
                    }

                // Fetch daily and hourly weather data
                weatherRepository.get5DayForecast(longitude, latitude)
                    .map { response -> response?.toDailyWeatherEntities(lon = longitude.toString(), lat = latitude.toString(), false) ?: emptyList() }
                    .collect { dailyWeatherEntities ->
                        weatherRepository.insertDailyWeather(dailyWeatherEntities)
                        _dailyWeatherState.value = DataState.Success(dailyWeatherEntities)
                    }

                weatherRepository.fetchHourlyWeatherData(longitude, latitude)
                    .map { response -> response?.toHourlyWeatherEntities(lon = longitude.toString(), lat = latitude.toString(), false) ?: emptyList() }
                    .collect { hourlyWeatherEntities ->
                        weatherRepository.insertHourlyWeather(hourlyWeatherEntities)
                        _hourlyWeatherState.value = DataState.Success(hourlyWeatherEntities)
                    }

                fetchWeatherFromRoom(longitude, latitude)
            } catch (e: Exception) {
                _weatherState.value = DataState.Error(R.string.error_fetching_favorite_weather_data)
            }
        }
    }

    private fun fetchWeatherFromRoom(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                launch {
                    weatherRepository.getHourlyWeather(latitude, longitude).collect { response ->
                        _hourlyWeatherState.value = DataState.Success(response)
                    }
                }
                launch {
                    weatherRepository.getDailyWeather(latitude, longitude).collect { response ->
                        _dailyWeatherState.value = DataState.Success(response)
                    }
                }
                launch {
                    weatherRepository.getWeather(latitude, longitude).collect { response ->
                        _weatherState.value = DataState.Success(response)
                    }
                }
            } catch (e: Exception) {
                _weatherState.value = DataState.Error(R.string.error_fetching_favorite_weather_data)
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


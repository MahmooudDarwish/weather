package com.example.weather.features.favorites.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.utils.model.API.toDailyWeatherEntities
import com.example.weather.utils.model.API.toHourlyWeatherEntities
import com.example.weather.utils.model.API.toWeatherEntity
import com.example.weather.utils.model.DataState
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.WeatherRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException

class FavoritesViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _favorites = MutableStateFlow<DataState<List<WeatherEntity?>>>(DataState.Loading)
    val favorites: StateFlow<DataState<List<WeatherEntity?>>>
        get() = _favorites

    private val supervisorJob = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        val errorMessage = when (exception) {
            is IOException -> R.string.network_error
            is HttpException -> R.string.server_error
            is TimeoutException -> R.string.timeout_error
            else -> R.string.unexpected_error
        }
        _favorites.value = DataState.Error(errorMessage)
    }


    fun updateWeatherAndRefreshRoom(latitude: Double, longitude: Double, city: String) {
        viewModelScope.launch(Dispatchers.IO + supervisorJob + exceptionHandler) {
            try {
                _favorites.value = DataState.Loading
                launch(Dispatchers.IO) {
                    weatherRepository.fetchWeatherData(longitude, latitude).map { response ->
                        response?.toWeatherEntity(
                            city,
                            lat = latitude.toString(),
                            lon = longitude.toString(),
                            isFavorite = true
                        )
                    }.collect { currentWeatherEntity ->
                        Log.d(
                            "FavoritesViewModel", "Current Weather Entity: $currentWeatherEntity"
                        )
                        currentWeatherEntity?.let {
                            weatherRepository.insertWeather(it)
                        }
                    }
                }

                launch(Dispatchers.IO) {
                    weatherRepository.get5DayForecast(longitude, latitude).map { response ->
                        response?.toDailyWeatherEntities(
                            lon = longitude.toString(), lat = latitude.toString(), true
                        ) ?: emptyList()
                    }.collect { dailyWeatherEntities ->
                        Log.d(
                            "FavoritesViewModel", "Daily Weather Entities: $dailyWeatherEntities"
                        )
                        weatherRepository.insertDailyWeather(dailyWeatherEntities)
                    }
                }

                launch(Dispatchers.IO) {
                    weatherRepository.fetchHourlyWeatherData(longitude, latitude).map { response ->
                        response?.toHourlyWeatherEntities(
                            lon = longitude.toString(), lat = latitude.toString(), true
                        ) ?: emptyList()
                    }.collect { hourlyWeatherEntities ->
                        weatherRepository.insertHourlyWeather(hourlyWeatherEntities)
                    }
                }

                //fetchAllFavoriteWeather()

            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error fetching or saving weather data", e)
                _favorites.value = DataState.Error(R.string.error_fetching_weather_data)
            }
        }
    }


    fun fetchAllFavoriteWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.getAllFavoriteWeather().collect { response ->
                    _favorites.value = DataState.Success(response)
                }
            } catch (e: Exception) {
                _favorites.value = DataState.Error(R.string.error_fetching_favorite_weather_data)
            }
        }
    }


    fun deleteFavorite(weatherEntity: WeatherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.deleteFavoriteWeather(
                    weatherEntity.longitude, weatherEntity.latitude
                )
            } catch (e: Exception) {
                _favorites.value = DataState.Error(R.string.error_deleting_favorite_weather_data)
            }
        }
    }
}

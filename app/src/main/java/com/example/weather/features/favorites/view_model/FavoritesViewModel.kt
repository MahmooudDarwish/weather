package com.example.weather.features.favorites.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.model.API.toDailyWeatherEntities
import com.example.weather.utils.model.API.toHourlyWeatherEntities
import com.example.weather.utils.model.API.toWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<WeatherEntity?>>(emptyList())
    val favorites: StateFlow<List<WeatherEntity?>>
        get() = _favorites

    fun updateWeatherAndRefreshRoom(latitude: Double, longitude: Double, city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Collect and save current weather data
                weatherRepository.fetchWeatherData(longitude, latitude)
                    .map { response -> response?.toWeatherEntity(city) }
                    .collect { currentWeatherEntity ->
                        Log.d("FavoritesViewModel", "Current Weather Entity: $currentWeatherEntity")
                        currentWeatherEntity?.let {
                            weatherRepository.insertFavoriteWeather(it)
                        }
                    }

                // Fetch and save daily weather
                weatherRepository.get5DayForecast(longitude, latitude)
                    .map { response -> response?.toDailyWeatherEntities() ?: emptyList() }
                    .collect { dailyWeatherEntities ->
                        Log.d("FavoritesViewModel", "Daily Weather Entities: $dailyWeatherEntities")
                        weatherRepository.insertFavoriteDailyWeather(dailyWeatherEntities)
                    }

                // Fetch and save hourly weather
                weatherRepository.fetchHourlyWeatherData(longitude, latitude)
                    .map { response -> response?.toHourlyWeatherEntities() ?: emptyList() }
                    .collect { hourlyWeatherEntities ->
                        weatherRepository.insertFavoriteHourlyWeather(hourlyWeatherEntities)
                    }

                fetchAllFavoriteWeather()
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error fetching or saving weather data", e)
            }
        }
    }

    fun fetchAllFavoriteWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.getAllFavoriteWeather().collect { response ->
                    _favorites.value = response
                }
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error fetching weather data from room", e)
            }
        }
    }


    fun deleteFavorite(weatherEntity: WeatherEntity) {
        viewModelScope.launch {
            weatherRepository.deleteFavoriteWeather(weatherEntity.longitude, weatherEntity.latitude)
        }
    }
}

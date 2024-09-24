package com.example.weather.features.favorites.view_model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.model.API.toDailyWeatherEntities
import com.example.weather.utils.model.API.toHourlyWeatherEntities
import com.example.weather.utils.model.API.toWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {

    private val _favorites = MutableLiveData<List<WeatherEntity?>>()
    val favorites: MutableLiveData<List<WeatherEntity?>>
        get() = _favorites

    fun getWeatherAndSaveToDatabase(latitude: Double, longitude: Double, city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Collect and save current weather data
                weatherRepository.fetchWeatherData(longitude, latitude).collect { currentWeather ->
                    currentWeather?.let {
                        weatherRepository.insertFavoriteWeather(it.toWeatherEntity(city))
                    }
                }
                // Collect and save daily weather data
                launch {
                    weatherRepository.get5DayForecast(longitude, latitude).collect { dailyWeather ->
                        dailyWeather?.let {
                            weatherRepository.insertFavoriteDailyWeather(it.toDailyWeatherEntities())
                        }
                    }
                }

                // Collect and save hourly weather data
                launch {
                    weatherRepository.fetchHourlyWeatherData(longitude, latitude).collect { hourlyWeather ->
                        hourlyWeather?.let {
                            weatherRepository.insertFavoriteHourlyWeather(it.toHourlyWeatherEntities())
                        }
                    }
                }


                // Update favorites list
                fetchAllFavoriteWeather()
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error fetching or saving weather data", e)
            }
        }
    }

    fun fetchAllFavoriteWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getAllFavoriteWeather().collect { response ->
                Log.d("FavoritesViewModel", "Response: $response")
                _favorites.postValue(response)
            }
        }
    }


    fun deleteFavorite(weatherEntity: WeatherEntity) {
        viewModelScope.launch {
            weatherRepository.deleteFavoriteWeather(weatherEntity.longitude, weatherEntity.latitude)
            fetchAllFavoriteWeather()  // Refresh the list after deletion

        }
    }
}

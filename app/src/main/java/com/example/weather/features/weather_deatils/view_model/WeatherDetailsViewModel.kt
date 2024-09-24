package com.example.weather.features.weather_deatils.view_model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed

import com.example.weather.utils.model.API.WeatherResponse
import com.example.weather.utils.model.API.toDailyWeatherEntities
import com.example.weather.utils.model.API.toHourlyWeatherEntities
import com.example.weather.utils.model.API.toWeatherEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherDetailsViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {

    private val _hourlyWeatherData = MutableLiveData<List<HourlyWeatherEntity?>>()
    val hourlyWeatherData: MutableLiveData<List<HourlyWeatherEntity?>>
        get() = _hourlyWeatherData

    private val _dailyWeatherData = MutableLiveData<List<DailyWeatherEntity?>>()
    val dailyWeatherData: MutableLiveData<List<DailyWeatherEntity?>>
        get() = _dailyWeatherData


    private val _favoriteWeatherData = MutableLiveData<WeatherEntity?>()
    val favoriteWeatherData: MutableLiveData<WeatherEntity?>
        get() = _favoriteWeatherData

    fun getWeatherAndRefreshRoom(latitude: Double, longitude: Double, city : String ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch current weather data from API
                weatherRepository.fetchWeatherData(longitude, latitude).collect { response ->
                    response?.let {
                        weatherRepository.insertFavoriteWeather(it.toWeatherEntity(city))
                    }
                }


            } catch (e: Exception) {
                Log.e("WeatherDetailsViewModel", "Error fetching or saving current weather data", e)
            }
        }
    }

    fun fetchHourlyWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch hourly weather data from API
                weatherRepository.fetchHourlyWeatherData(longitude, latitude).collect { response ->
                    response?.let {
                        Log.d("WeatherDetailsViewModel", "Hourly weather data: $it")
                        weatherRepository.insertFavoriteHourlyWeather(it.toHourlyWeatherEntities())
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherDetailsViewModel", "Error fetching or saving hourly weather data", e)
            }
        }
    }

    fun fetchDailyWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.get5DayForecast(longitude, latitude).collect { response ->
                    response?.let {
                        Log.d("WeatherDetailsViewModel", "Daily weather data: $it")
                        weatherRepository.insertFavoriteDailyWeather(it.toDailyWeatherEntities())
                    }
                }
            } catch (e: Exception) {
                Log.e("WeatherDetailsViewModel", "Error fetching or saving daily weather data", e)
            }
        }
    }

    fun fetchFavoriteWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                launch {
                    weatherRepository.getFavoriteHourlyWeather(longitude, latitude).collect { response ->
                        Log.d("WeatherDetailsViewModel", "Favorite hourly weather data: $response")
                        _hourlyWeatherData.postValue(response)
                    }
                }
                launch {
                    weatherRepository.getFavoriteDailyWeather(longitude, latitude).collect { response ->
                        Log.d("WeatherDetailsViewModel", "Favorite daily weather data: $response")
                        _dailyWeatherData.postValue(response)
                    }
                }

                weatherRepository.getFavoriteWeather(longitude, latitude).collect { response ->
                    Log.d("WeatherDetailsViewModel", "Favorite weather data: $response")
                    _favoriteWeatherData.postValue(response)
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
}

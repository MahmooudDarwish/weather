package com.example.weather.features.weather_deatils.view_model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.API.HourlyWeatherResponse
import com.example.weather.utils.model.API.WeatherResponse
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherDetailsViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {
    private val _hourlyWeatherData = MutableLiveData<HourlyWeatherResponse?>()
    val hourlyWeatherData: MutableLiveData<HourlyWeatherResponse?>
        get() = _hourlyWeatherData


    private val _dailyWeatherData = MutableLiveData<DailyWeatherResponse?>()
    val dailyWeatherData: MutableLiveData<DailyWeatherResponse?>
        get() = _dailyWeatherData


    private val _favoriteWeatherData = MutableLiveData<WeatherEntity?>()
    val favoriteWeatherData: MutableLiveData<WeatherEntity?>
        get() = _favoriteWeatherData


    fun fetchHourlyWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.fetchHourlyWeatherData(longitude, latitude).collect { response ->
                _hourlyWeatherData.postValue(response)
            }
        }
    }

    fun fetchDailyWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.get5DayForecast(longitude, latitude).collect { response ->
                _dailyWeatherData.postValue(response)
            }
        }
    }

    fun fetchFavoriteWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getFavoriteWeather(longitude, latitude).collect { response ->
                _favoriteWeatherData.postValue(response)
            }
        }
    }

    private val _currentWeather = MutableLiveData<WeatherResponse?>()
    val currentWeather: MutableLiveData<WeatherResponse?>
        get() = _currentWeather

    fun getWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.fetchWeatherData(longitude, latitude).collect { response ->
                Log.d("HomeViewModel", "Response: $response")
                _currentWeather.postValue(response)
            }
        }
    }

    fun getWeatherMeasure(): Temperature {
        Log.d("HomeViewModel", "getLocationStatus called ${weatherRepository.getLocationStatus()}")
        return weatherRepository.getTemperatureUnit()
    }

    fun getWindMeasure(): WindSpeed {
        return weatherRepository.getWindSpeedUnit()
    }

}
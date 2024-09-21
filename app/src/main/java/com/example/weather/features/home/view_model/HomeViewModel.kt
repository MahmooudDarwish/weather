package com.example.weather.features.home.view_model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.model.ForecastResponse
import com.example.weather.utils.model.HourlyWeatherResponse
import com.example.weather.utils.model.WeatherRepositoryImpl
import com.example.weather.utils.model.WeatherResponse
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch

class HomeViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {

    private val _currentWeather = MutableLiveData<WeatherResponse?>()
    val currentWeather: MutableLiveData<WeatherResponse?>
        get() = _currentWeather


    private val _hourlyWeatherData = MutableLiveData<HourlyWeatherResponse?>()
    val hourlyWeatherData: MutableLiveData<HourlyWeatherResponse?>
        get() = _hourlyWeatherData


    private val _dailyWeatherData = MutableLiveData<ForecastResponse?>()
    val dailyWeatherData: MutableLiveData<ForecastResponse?>
        get() = _dailyWeatherData


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
                Log.d("HomeViewModel", "[forest Response: $response")
                _dailyWeatherData.postValue(response)
            }
        }
    }

    fun getWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.fetchAndStoreWeatherData(longitude, latitude).collect { response ->
                Log.d("HomeViewModel", "Response: $response")
                _currentWeather.postValue(response)
            }
        }
    }

    fun getLocationStatus(): LocationStatus {
        Log.d("HomeViewModel", "getLocationStatus called ${weatherRepository.getLocationStatus()}")
        return weatherRepository.getLocationStatus()
    }

    fun saveCurrentLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            weatherRepository.saveCurrentLocation(latitude, longitude)
        }
    }

    fun getCurrentLocation(): Pair<Double, Double>? {
        return weatherRepository.getCurrentLocation()
    }

}
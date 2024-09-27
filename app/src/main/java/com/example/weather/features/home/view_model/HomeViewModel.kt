package com.example.weather.features.home.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.model.API.ApiResponse
import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.API.HourlyWeatherResponse
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.model.API.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.launch

class HomeViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {

    // StateFlows for weather data with ApiResponse
    private val _currentWeatherState = MutableStateFlow<ApiResponse<WeatherResponse?>>(ApiResponse.Loading)
    val currentWeatherState: StateFlow<ApiResponse<WeatherResponse?>>
        get() = _currentWeatherState

    private val _hourlyWeatherState = MutableStateFlow<ApiResponse<HourlyWeatherResponse?>>(ApiResponse.Loading)
    val hourlyWeatherState: StateFlow<ApiResponse<HourlyWeatherResponse?>>
        get() = _hourlyWeatherState

    private val _dailyWeatherState = MutableStateFlow<ApiResponse<DailyWeatherResponse?>>(ApiResponse.Loading)
    val dailyWeatherState: StateFlow<ApiResponse<DailyWeatherResponse?>>
        get() = _dailyWeatherState


    fun getWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentWeatherState.value = ApiResponse.Loading
            try {
                weatherRepository.fetchWeatherData(longitude, latitude)
                    .map { response -> ApiResponse.Success(response) }
                    .collect { apiResponse ->
                        _currentWeatherState.value = apiResponse
                    }
            } catch (e: Exception) {
                _currentWeatherState.value = ApiResponse.Error(R.string.error_fetching_weather_data)
            }
        }
    }

    fun fetchHourlyWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            _hourlyWeatherState.value = ApiResponse.Loading
            try {
                weatherRepository.fetchHourlyWeatherData(longitude, latitude)
                    .map { response -> ApiResponse.Success(response) }
                    .collect { apiResponse ->
                        _hourlyWeatherState.value = apiResponse
                    }
            } catch (e: Exception) {
                _hourlyWeatherState.value = ApiResponse.Error(R.string.error_fetching_weather_data)
            }
        }
    }
    fun fetchDailyWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            _dailyWeatherState.value = ApiResponse.Loading
            try {
                weatherRepository.get5DayForecast(longitude, latitude)
                    .map { response -> ApiResponse.Success(response) }
                    .collect { apiResponse ->
                        _dailyWeatherState.value = apiResponse
                    }
            } catch (e: Exception) {
                _dailyWeatherState.value = ApiResponse.Error(R.string.error_fetching_weather_data)
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
            weatherRepository.saveCurrentLocation(latitude, longitude)
        }
    }

    fun getCurrentLocation(): Pair<Double, Double>? {
        return weatherRepository.getCurrentLocation()
    }

}
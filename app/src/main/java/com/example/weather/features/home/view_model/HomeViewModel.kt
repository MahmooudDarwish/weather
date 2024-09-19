package com.example.weather.features.home.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.model.WeatherRepositoryImpl
import com.example.weather.utils.model.WeatherResponse
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch

class HomeViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {

    private val _currentWeather = MutableLiveData<WeatherResponse>()
    val currentWeather: LiveData<WeatherResponse>
        get() = _currentWeather

    fun getWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.fetchAndStoreWeatherData(longitude, latitude).collect { response ->
                Log.d("HomeViewModel", "Response: $response")
                _currentWeather.postValue(response)
            }
        }
    }
}
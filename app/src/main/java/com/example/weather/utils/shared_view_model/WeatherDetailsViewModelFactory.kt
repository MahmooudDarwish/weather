package com.example.weather.utils.shared_view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weather.utils.model.repository.WeatherRepository
import com.example.weather.utils.model.repository.WeatherRepositoryImpl

class WeatherDetailsViewModelFactory(
    private val weatherRepository: WeatherRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherDetailsViewModel::class.java)) {
            return WeatherDetailsViewModel(weatherRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

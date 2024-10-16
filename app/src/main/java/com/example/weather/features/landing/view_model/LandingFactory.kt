package com.example.weather.features.landing.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weather.utils.model.repository.WeatherRepository
import com.example.weather.utils.model.repository.WeatherRepositoryImpl

class LandingFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(LandingViewModel::class.java)) {
            LandingViewModel(repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }

}
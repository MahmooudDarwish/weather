package com.example.weather.features.alarm.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weather.utils.model.repository.WeatherRepositoryImpl

class AlarmViewModelFactory (
    private val weatherRepository: WeatherRepositoryImpl
    ) : ViewModelProvider.Factory
    {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
                return AlarmViewModel(weatherRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


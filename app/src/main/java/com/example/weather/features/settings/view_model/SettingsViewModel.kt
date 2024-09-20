package com.example.weather.features.settings.view_model

import androidx.lifecycle.ViewModel
import com.example.weather.utils.model.WeatherRepository

import androidx.lifecycle.viewModelScope
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.Location
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: WeatherRepository) : ViewModel() {

    fun saveNotificationStatus(status: Boolean) {
        viewModelScope.launch {
            repository.setNotificationStatus(status)
        }
    }

    fun getNotificationStatus(): Boolean {
        return repository.getNotificationStatus()
    }

    fun saveLanguage(lang: Language) {
        viewModelScope.launch {
            repository.setLanguage(lang)
        }
    }

    fun getLanguage(): Language {
        return repository.getLanguage()
    }

    fun saveLocationStatus(location: Location) {
        viewModelScope.launch {
            repository.setLocationStatus(location)
        }
    }

    fun getLocationStatus(): Location {
        return repository.getLocationStatus()
    }

    fun saveTemperatureUnit(unit: Temperature) {
        viewModelScope.launch {
            repository.setTemperatureUnit(unit)
        }
    }

    fun getTemperatureUnit(): Temperature {
        return repository.getTemperatureUnit()
    }

    fun saveWindSpeedUnit(unit: WindSpeed) {
        viewModelScope.launch {
            repository.setWindSpeedUnit(unit)
        }
    }

    fun getWindSpeedUnit(): WindSpeed {
        return repository.getWindSpeedUnit()
    }
}

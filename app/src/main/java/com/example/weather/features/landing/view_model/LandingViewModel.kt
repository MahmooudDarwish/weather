package com.example.weather.features.landing.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.model.repository.WeatherRepository
import kotlinx.coroutines.launch


class LandingViewModel(val repository: WeatherRepository) : ViewModel() {
    fun saveCurrentLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.saveCurrentLocation(latitude, longitude)
        }
    }

    fun getCurrentLocation(): Pair<Double, Double>? {
        return repository.getCurrentLocation()
    }

    fun saveLocationStatus(locationStatus: LocationStatus) {
        viewModelScope.launch {
            repository.setLocationStatus(locationStatus)
        }
    }

    fun getLocationStatus(): LocationStatus {
        return repository.getLocationStatus()
    }
    fun isFirstLaunch(): Boolean {
        return repository.isFirstLaunch()
    }

    fun setFirstLaunchCompleted(){
        viewModelScope.launch {
            repository.setFirstLaunchCompleted()
        }
    }

    fun saveNotificationStatus(status: Boolean) {
        viewModelScope.launch {
            repository.setNotificationStatus(status)
        }
    }
}

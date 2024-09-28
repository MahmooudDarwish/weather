package com.example.weather.features.landing.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.managers.SharedDataManager
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.model.repository.WeatherRepository
import kotlinx.coroutines.launch


class LandingViewModel(val repository: WeatherRepository) : ViewModel() {

    init {
        viewModelScope.launch {
            val location = getCurrentLocation()
            SharedDataManager.emitLocation(location)
        }
    }

    fun saveCurrentLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val location = Pair(latitude, longitude)
            SharedDataManager.emitLocation(location)
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

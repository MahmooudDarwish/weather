package com.example.weather.features.landing.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.utils.model.WeatherRepository


class LandingViewModel(val repository: WeatherRepository): ViewModel() {

    private val _locationData = MutableLiveData<Pair<Double, Double>>()
    val locationData: LiveData<Pair<Double, Double>> get() = _locationData

    private val _isGpsSelected = MutableLiveData<Boolean>()

    private val _isMapSelected = MutableLiveData<Boolean>()

    private val _isNotificationsEnabled = MutableLiveData<Boolean>()

    init {
        _isGpsSelected.value = true
        _isMapSelected.value = false
        _isNotificationsEnabled.value = false
    }

    fun setLocationData(latitude: Double, longitude: Double) {
        _locationData.value = Pair(latitude, longitude)
    }
    fun selectGps(selected: Boolean) {
        _isGpsSelected.value = selected
    }

    fun selectMap(selected: Boolean) {
        _isMapSelected.value = selected
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _isNotificationsEnabled.value = enabled
    }

}

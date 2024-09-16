package com.example.weather.features.landing.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LandingViewModel : ViewModel() {

    private val _isGpsSelected = MutableLiveData<Boolean>()
    val isGpsSelected: LiveData<Boolean> get() = _isGpsSelected

    private val _isMapSelected = MutableLiveData<Boolean>()
    val isMapSelected: LiveData<Boolean> get() = _isMapSelected

    private val _isNotificationsEnabled = MutableLiveData<Boolean>()
    val isNotificationsEnabled: LiveData<Boolean> get() = _isNotificationsEnabled

    init {
        _isGpsSelected.value = false
        _isMapSelected.value = false
        _isNotificationsEnabled.value = false
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

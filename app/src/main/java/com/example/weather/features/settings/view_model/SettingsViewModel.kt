package com.example.weather.features.settings.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.managers.SharedDataManager
import com.example.weather.utils.model.repository.WeatherRepository
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: WeatherRepository) : ViewModel() {

    val languageFlow: SharedFlow<Language> = SharedDataManager.languageFlow

    private val _locationStatusFlow = MutableStateFlow(LocationStatus.MAP)
    val locationStatusFlow: StateFlow<LocationStatus> = _locationStatusFlow

    private val _temperatureFlow = MutableStateFlow(Temperature.CELSIUS)
    val temperatureFlow: StateFlow<Temperature> = _temperatureFlow

    private val _windSpeedFlow = MutableStateFlow(WindSpeed.METERS_PER_SECOND)
    val windSpeedFlow: StateFlow<WindSpeed> = _windSpeedFlow

    private val _notificationStatusFlow = MutableStateFlow(false)
    val notificationStatusFlow: StateFlow<Boolean> = _notificationStatusFlow

    init {
        viewModelScope.launch {
            val language = repository.getLanguage()
            SharedDataManager.emitLanguage(language)
            _locationStatusFlow.emit(repository.getLocationStatus())
            _temperatureFlow.emit(repository.getTemperatureUnit())
            _windSpeedFlow.emit(repository.getWindSpeedUnit())
            _notificationStatusFlow.emit(repository.getNotificationStatus())
        }
    }

    fun saveNotificationStatus(status: Boolean) {
        viewModelScope.launch {
            repository.setNotificationStatus(status)
            _notificationStatusFlow.emit(status)
        }
    }

    fun saveLanguage(lang: Language) {
        viewModelScope.launch {
            repository.setLanguage(lang)
            SharedDataManager.emitLanguage(lang)
        }
    }

    fun saveLocationStatus(locationStatus: LocationStatus) {
        viewModelScope.launch {
            repository.setLocationStatus(locationStatus)
            _locationStatusFlow.emit(locationStatus)
        }
    }

    fun saveTemperatureUnit(unit: Temperature) {
        viewModelScope.launch {
            repository.setTemperatureUnit(unit)
            _temperatureFlow.emit(unit)
        }
    }

    fun saveWindSpeedUnit(unit: WindSpeed) {
        viewModelScope.launch {
            repository.setWindSpeedUnit(unit)
            _windSpeedFlow.emit(unit)
        }
    }

    fun saveCurrentLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val location = Pair(latitude, longitude)
            SharedDataManager.emitLocation(location)
            repository.saveCurrentLocation(latitude, longitude)
        }
    }
}

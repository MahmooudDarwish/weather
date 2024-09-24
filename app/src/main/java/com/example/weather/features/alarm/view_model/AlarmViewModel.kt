package com.example.weather.features.alarm.view_model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {

    private val _alerts = MutableLiveData<List<AlarmEntity?>>()
    val alerts: MutableLiveData<List<AlarmEntity?>>
        get() = _alerts

    fun getAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Collect and save current weather data
                weatherRepository.getAllAlarms().collect { alarms ->
                    alerts.postValue(alarms)
                }
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error fetching or saving weather data", e)
            }
        }
    }

    fun addAlert(alert: AlarmEntity) {
        viewModelScope.launch {
            weatherRepository.insertAlarm(alert)
            getAlerts()
        }
    }


    fun deleteAlert(alert: AlarmEntity) {
        viewModelScope.launch {
            weatherRepository.deleteAlarm(alert)
            getAlerts()

        }
    }
}

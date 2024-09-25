package com.example.weather.features.alarm.view_model

import android.app.AlarmManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.utils.model.API.toDailyWeatherEntities
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.toDateString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {

    private val _alerts = MutableLiveData<List<AlarmEntity?>>()
    val alerts: MutableLiveData<List<AlarmEntity?>>
        get() = _alerts

    private val _weatherData = MutableStateFlow<List<DailyWeatherEntity>>(emptyList())
    val weatherData: StateFlow<List<DailyWeatherEntity>>
        get() = _weatherData

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

    fun fetch30DayWeather() {
        val location : Pair<Double, Double>? = getCurrentLocation()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.get30DayForecast(location!!.second, location.first).collect { response ->
                    if (response != null) {
                        val dailyWeatherEntities = response.toDailyWeatherEntities()
                        _weatherData.value = dailyWeatherEntities
                    }
                }
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Error fetching 30-day weather data", e)
            }
        }
    }


    private fun getCurrentLocation(): Pair<Double, Double>? {
        return weatherRepository.getCurrentLocation()
    }




    fun addAlert(alert: AlarmEntity) {
        viewModelScope.launch {
            Log.d("AlarmViewModel", "Adding alert: $alert")
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

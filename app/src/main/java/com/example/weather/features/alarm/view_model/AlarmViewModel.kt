package com.example.weather.features.alarm.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.utils.model.API.ApiResponse
import com.example.weather.utils.model.API.toDailyWeatherEntities
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val weatherRepository: WeatherRepositoryImpl
) : ViewModel() {

    private val _alerts = MutableStateFlow<List<AlarmEntity?>>(emptyList())
    val alerts: StateFlow<List<AlarmEntity?>>
        get() = _alerts

    private val _weatherDataState = MutableStateFlow<ApiResponse<List<DailyWeatherEntity>>>(ApiResponse.Loading)
    val weatherDataState: StateFlow<ApiResponse<List<DailyWeatherEntity>>>
        get() = _weatherDataState

    fun getAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.getAllAlarms().collect { alarms ->
                    _alerts.value = alarms
                }
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Error fetching or saving weather data", e)
            }
        }
    }

        fun fetch30DayWeather() {
            val location: Pair<Double, Double>? = getCurrentLocation()

            viewModelScope.launch(Dispatchers.IO) {
                _weatherDataState.value = ApiResponse.Loading

                try {
                    val weatherFlow = weatherRepository
                        .get30DayForecast(location!!.second, location.first)
                        .map { response ->
                            response?.toDailyWeatherEntities(lon = location.second.toString(), lat = location.first.toString(),   isFavourite = false) ?: emptyList()
                        }

                    weatherFlow.collect { dailyWeatherEntities ->
                        Log.d("AlarmViewModel", "Weather data fetched: 11111")
                        _weatherDataState.value = ApiResponse.Success(dailyWeatherEntities)
                    }

                } catch (e: Exception) {
                    _weatherDataState.value = ApiResponse.Error(R.string.error_fetching_weather_data)
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


    fun deleteAlert(id: Long) {
        viewModelScope.launch {
            weatherRepository.deleteAlarm(id)
            getAlerts()

        }
    }
}

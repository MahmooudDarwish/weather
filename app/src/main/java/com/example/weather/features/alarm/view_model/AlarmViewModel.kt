package com.example.weather.features.alarm.view_model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.utils.model.DataState
import com.example.weather.utils.model.API.toDailyWeatherEntities
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.repository.WeatherRepository
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeoutException

class AlarmViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _alerts = MutableStateFlow<List<AlarmEntity?>>(emptyList())
    val alerts: StateFlow<List<AlarmEntity?>>
        get() = _alerts

    private val _weatherDataState =
        MutableStateFlow<DataState<List<DailyWeatherEntity>>>(DataState.Loading)
    val weatherDataState: StateFlow<DataState<List<DailyWeatherEntity>>>
        get() = _weatherDataState


        fun getAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.getAllAlarms().collect { alarms ->
                    _alerts.value = alarms
                }
            } catch (e: Exception) {
                _weatherDataState.value = DataState.Error(R.string.error_fetching_alerts)
            }
        }
    }


    fun fetch30DayWeather() {
        val location: Pair<Double, Double>? = getCurrentLocation()

        viewModelScope.launch(Dispatchers.IO) {
            _weatherDataState.value = DataState.Loading

            try {
                val weatherFlow = weatherRepository
                    .get30DayForecast(location!!.second, location.first)
                    .map { response ->
                        response?.toDailyWeatherEntities(
                            lon = location.second.toString(),
                            lat = location.first.toString(),
                            isFavourite = false
                        ) ?: emptyList()
                    }

                weatherFlow.collect { dailyWeatherEntities ->
                    Log.d("AlarmViewModel", "Weather data fetched: 11111")
                    _weatherDataState.value = DataState.Success(dailyWeatherEntities)
                }

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is IOException -> R.string.network_error
                    is HttpException -> R.string.server_error
                    is TimeoutException -> R.string.timeout_error
                    else -> R.string.unexpected_error
                }
                _weatherDataState.value = DataState.Error(errorMessage)
                Log.e("AlarmViewModel", "Error fetching 30-day weather data", e)
            }
        }
    }


    private fun getCurrentLocation(): Pair<Double, Double>? {
        return weatherRepository.getCurrentLocation()
    }


    fun addAlert(alert: AlarmEntity) {
        if (alert.title.isEmpty() || alert.description.isEmpty() || alert.startDate == 0L) {
            _weatherDataState.value = DataState.Error(R.string.invalid_alert_data)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.insertAlarm(alert)
            } catch (e: Exception) {
                _weatherDataState.value = DataState.Error(R.string.error_adding_alarm_data)
            }
        }
    }


    fun deleteAlert(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.deleteAlarm(id)
            } catch (e: Exception) {
                _weatherDataState.value = DataState.Error(R.string.error_deleting_alarm_data)

            }
        }
    }
}

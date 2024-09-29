package com.example.weather.utils.model.repository

import android.util.Log
import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.API.HourlyWeatherResponse
import com.example.weather.utils.model.API.WeatherResponse
import com.example.weather.utils.remote.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class FakeRemoteDataSource(
    private val mockedHourlyWeatherResponse: MutableList<HourlyWeatherResponse>,
    private val mockedDailyWeatherResponse: MutableList<DailyWeatherResponse>,
    private val mockedWeatherResponse: MutableList<WeatherResponse>
) : WeatherRemoteDataSource {
    override fun getCurrentWeather(
        latitude: String,
        longitude: String,
        lang: String
    ): Flow<WeatherResponse?> {
        return flow {
            val lat = latitude.toDoubleOrNull()
            val lon = longitude.toDoubleOrNull()

            if (lat != null && lon != null) {
                val weatherResponse = mockedWeatherResponse.find {
                    it.coord.lat == lat && it.coord.lon == lon
                }
                emit(weatherResponse)
            } else {
                emit(mockedWeatherResponse[0])
            }
        }
    }


    override fun getHourlyWeather(
        latitude: String,
        longitude: String,
        lang: String
    ): Flow<HourlyWeatherResponse?> {
        TODO("Not yet implemented")
    }

    override fun get5DayForecast(
        latitude: String,
        longitude: String,
        lang: String
    ): Flow<DailyWeatherResponse?> {
        TODO("Not yet implemented")
    }

    override fun get30DayForecast(
        latitude: String,
        longitude: String,
        lang: String
    ): Flow<DailyWeatherResponse?> {
        TODO("Not yet implemented")
    }
}
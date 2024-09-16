package com.example.weather.utils.remote

import android.util.Log
import com.example.weather.utils.model.WeatherResponse


class WeatherRemoteDataSourceImpl private constructor() : WeatherRemoteDataSource {

    private val apiService: WeatherApiService = RetroFitInstance.api
    private var WEATHER_API_KEY: String = System.getenv("WEATHER_API_KEY")!!.toString()


    companion object {
        @Volatile
        private var instance: WeatherRemoteDataSourceImpl? = null

        fun getInstance(): WeatherRemoteDataSourceImpl {
            return instance ?: synchronized(this) {
                instance ?: WeatherRemoteDataSourceImpl().also { instance = it }
            }
        }
    }
    override fun getCurrentWeather(
        latitude: String, longitude: String, metric: String, lang: String
    ): WeatherResponse? {
        val response = apiService.getCurrentWeather(
            lat = latitude,
            lon = longitude,
            apiKey = WEATHER_API_KEY,
            units = metric,
            lang = lang,
            )
        Log.i("WeatherRemoteDataSourceImpl", "getCurrentWeather: $response")
        return response.body()
    }
}
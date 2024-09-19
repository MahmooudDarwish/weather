package com.example.weather.utils.remote

import android.util.Log
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Query


class WeatherRemoteDataSourceImpl private constructor() : WeatherRemoteDataSource {

    private val apiService: WeatherApiService = RetroFitInstance.api

    private val TAG = "WeatherRemoteDataSource"
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
    ): Flow<WeatherResponse?> = flow {
        try {

            val response = apiService.getCurrentWeather(
                latitude,
                longitude,
                lang,
                metric,

            )
            if (response.isSuccessful) {
                Log.i(TAG, "getCurrentWeatherSucess: ${response.body()?.id}")
                emit(response.body())
            } else {
                val errorBody = response.errorBody()?.string()
                Log.d(TAG, "getCurrentWeatherError: $errorBody")
                emit(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentWeatherException: ${e.message.toString()}")
            emit(null)
        }
    }
}
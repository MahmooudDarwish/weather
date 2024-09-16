package com.example.weather.utils.remote

import com.example.weather.utils.constants.Keys
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetroFitInstance {
    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Keys.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
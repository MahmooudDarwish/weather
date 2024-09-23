package com.example.weather.utils.model.Local

import androidx.room.TypeConverter
import com.example.weather.utils.model.Coordinates
import com.example.weather.utils.model.Main
import com.example.weather.utils.model.Weather
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromWeatherList(value: List<Weather>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toWeatherList(value: String): List<Weather> {
        val gson = Gson()
        val listType = object : TypeToken<List<Weather>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromCoordinates(coordinates: Coordinates): String {
        val gson = Gson()
        return gson.toJson(coordinates)
    }

    @TypeConverter
    fun toCoordinates(value: String): Coordinates {
        return Gson().fromJson(value, Coordinates::class.java)
    }

    @TypeConverter
    fun fromMain(main: Main): String {
        val gson = Gson()
        return gson.toJson(main)
    }

    @TypeConverter
    fun toMain(value: String): Main {
        return Gson().fromJson(value, Main::class.java)
    }

    // Similarly, converters for Sys, Clouds, Wind, etc.
}

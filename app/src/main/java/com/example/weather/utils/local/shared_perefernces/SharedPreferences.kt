package com.example.weather.utils.local.shared_perefernces

import android.content.Context

class SettingsPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)

    fun setLanguage(lang: String) {
        sharedPreferences.edit().putString("lang", lang).apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString("lang", "en") ?: "en"
    }

    fun setTemperatureUnit(unit: String) {
        sharedPreferences.edit().putString("temp_unit", unit).apply()
    }

    fun getTemperatureUnit(): String {
        return sharedPreferences.getString("temp_unit", "metric") ?: "metric"
    }

    fun setWindSpeedUnit(unit: String) {
        sharedPreferences.edit().putString("wind_unit", unit).apply()
    }

    fun getWindSpeedUnit(): String {
        return sharedPreferences.getString("wind_unit", "m/s") ?: "m/s"
    }
}

package com.example.weather.utils.local.shared_perefernces


import android.content.Context
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.Location
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.constants.Keys

class SharedPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(Keys.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    // Language settings
    fun setLanguage(lang: Language) {
        sharedPreferences.edit().putString(Keys.LANGUAGE_KEY, lang.name).apply()
    }

    fun getLanguage(): Language {
        val lang = sharedPreferences.getString(Keys.LANGUAGE_KEY, Keys.DEFAULT_LANGUAGE) ?: Keys.DEFAULT_LANGUAGE
        return Language.valueOf(lang)
    }

    // Temperature Unit settings
    fun setTemperatureUnit(unit: Temperature) {
        sharedPreferences.edit().putString(Keys.TEMPERATURE_UNIT_KEY, unit.name).apply()
    }

    fun getTemperatureUnit(): Temperature {
        val tempUnit = sharedPreferences.getString(Keys.TEMPERATURE_UNIT_KEY, Temperature.CELSIUS.name) ?: Keys.DEFAULT_TEMPERATURE_UNIT
        return Temperature.valueOf(tempUnit)
    }

    // Wind Speed Unit settings
    fun setWindSpeedUnit(unit: WindSpeed) {
        sharedPreferences.edit().putString(Keys.WIND_SPEED_UNIT_KEY, unit.name).apply()
    }

    fun getWindSpeedUnit(): WindSpeed {
        val windUnit = sharedPreferences.getString(Keys.WIND_SPEED_UNIT_KEY, WindSpeed.METERS_PER_SECOND.name) ?: Keys.DEFAULT_WIND_SPEED_UNIT
        return WindSpeed.valueOf(windUnit)
    }

    // Location settings
    fun setLocation(location: Location) {
        sharedPreferences.edit().putString(Keys.LOCATION_KEY, location.name).apply()
    }

    fun getLocation(): Location {
        val location = sharedPreferences.getString(Keys.LOCATION_KEY, Location.GPS.name) ?: Location.GPS.name
        return Location.valueOf(location)
    }

    // Notification settings
    fun setNotificationStatus(status: Boolean) {
        sharedPreferences.edit().putBoolean(Keys.NOTIFICATION_STATUS_KEY, status).apply()
    }

    fun getNotificationStatus(): Boolean {
        return sharedPreferences.getBoolean(Keys.NOTIFICATION_STATUS_KEY, false)
    }
}

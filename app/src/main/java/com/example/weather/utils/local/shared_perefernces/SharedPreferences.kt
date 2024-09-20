package com.example.weather.utils.local.shared_perefernces


import android.content.Context
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.constants.Keys

class SharedPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(Keys.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    //landing options
    fun setFirstLaunchCompleted(status: Boolean) {
        sharedPreferences.edit().putBoolean(Keys.LANDING_OPTION_KEY, status).apply()
    }

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(Keys.LANDING_OPTION_KEY, false)
    }

    // Language settings
    fun setLanguage(lang: Language) {
        sharedPreferences.edit().putString(Keys.LANGUAGE_KEY, lang.name).apply()
    }

    fun getLanguage(): Language {
        val lang = sharedPreferences.getString(Keys.LANGUAGE_KEY, Language.ENGLISH.name) ?: Language.ENGLISH.name
        return Language.valueOf(lang)
    }

    // Temperature Unit settings
    fun setTemperatureUnit(unit: Temperature) {
        sharedPreferences.edit().putString(Keys.TEMPERATURE_UNIT_KEY, unit.name).apply()
    }

    fun getTemperatureUnit(): Temperature {
        val tempUnit = sharedPreferences.getString(Keys.TEMPERATURE_UNIT_KEY, Temperature.CELSIUS.name) ?: Temperature.CELSIUS.name
        return Temperature.valueOf(tempUnit)
    }

    // Wind Speed Unit settings
    fun setWindSpeedUnit(unit: WindSpeed) {
        sharedPreferences.edit().putString(Keys.WIND_SPEED_UNIT_KEY, unit.name).apply()
    }

    fun getWindSpeedUnit(): WindSpeed {
        val windUnit = sharedPreferences.getString(Keys.WIND_SPEED_UNIT_KEY, WindSpeed.METERS_PER_SECOND.name) ?: WindSpeed.METERS_PER_SECOND.name
        return WindSpeed.valueOf(windUnit)
    }

    // Location settings
    fun setLocationStatus(locationStatus: LocationStatus) {
        sharedPreferences.edit().putString(Keys.LOCATION_KEY, locationStatus.name).apply()
    }
    fun getLocationStatus(): LocationStatus {
        val locationStatus = sharedPreferences.getString(Keys.LOCATION_KEY, LocationStatus.MAP.name) ?: LocationStatus.MAP.name
        return LocationStatus.valueOf(locationStatus)
    }

    // Notification settings
    fun setNotificationStatus(status: Boolean) {
        sharedPreferences.edit().putBoolean(Keys.NOTIFICATION_STATUS_KEY, status).apply()
    }

    fun getNotificationStatus(): Boolean {
        return sharedPreferences.getBoolean(Keys.NOTIFICATION_STATUS_KEY, false)
    }


    //Current location
    fun setLocation(latitude: Double, longitude: Double) {
        with(sharedPreferences.edit()) {
            putFloat(Keys.LATITUDE_KEY, latitude.toFloat())
            putFloat(Keys.LONGITUDE_KEY, longitude.toFloat())
            apply()
        }
    }

    fun getLocation(): Pair<Double, Double>? {
        val latitude = sharedPreferences.getFloat(Keys.LATITUDE_KEY, Float.NaN)
        val longitude = sharedPreferences.getFloat(Keys.LONGITUDE_KEY, Float.NaN)
        return if (latitude.isNaN() || longitude.isNaN()) {
            null
        } else {
            Pair(latitude.toDouble(), longitude.toDouble())
        }
    }

}

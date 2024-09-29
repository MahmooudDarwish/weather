package com.example.weather.utils.local.shared_perefernces


import android.content.SharedPreferences
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.constants.Keys
import java.lang.Double.parseDouble

class SharedPreferencesManager(val sharedPreferences: SharedPreferences) : ISharedPreferencesManager {

    //landing options
    override fun setFirstLaunchCompleted(status: Boolean) {
        sharedPreferences.edit().putBoolean(Keys.LANDING_OPTION_KEY, status).apply()
    }

    override fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(Keys.LANDING_OPTION_KEY, false)
    }

    // Language settings
    override fun setLanguage(lang: Language) {
        sharedPreferences.edit().putString(Keys.LANGUAGE_KEY, lang.name).apply()
    }

    override fun getLanguage(): Language {
        val lang = sharedPreferences.getString(Keys.LANGUAGE_KEY, Language.ENGLISH.name) ?: Language.ENGLISH.name
        return Language.valueOf(lang)
    }

    // Temperature Unit settings
    override fun setTemperatureUnit(unit: Temperature) {
        sharedPreferences.edit().putString(Keys.TEMPERATURE_UNIT_KEY, unit.name).apply()
    }

    override fun getTemperatureUnit(): Temperature {
        val tempUnit = sharedPreferences.getString(Keys.TEMPERATURE_UNIT_KEY, Temperature.CELSIUS.name) ?: Temperature.CELSIUS.name
        return Temperature.valueOf(tempUnit)
    }

    // Wind Speed Unit settings
    override fun setWindSpeedUnit(unit: WindSpeed) {
        sharedPreferences.edit().putString(Keys.WIND_SPEED_UNIT_KEY, unit.name).apply()
    }

    override fun getWindSpeedUnit(): WindSpeed {
        val windUnit = sharedPreferences.getString(Keys.WIND_SPEED_UNIT_KEY, WindSpeed.METERS_PER_SECOND.name) ?: WindSpeed.METERS_PER_SECOND.name
        return WindSpeed.valueOf(windUnit)
    }

    // Location settings
    override fun setLocationStatus(locationStatus: LocationStatus) {
        sharedPreferences.edit().putString(Keys.LOCATION_KEY, locationStatus.name).apply()
    }
    override fun getLocationStatus(): LocationStatus {
        val locationStatus = sharedPreferences.getString(Keys.LOCATION_KEY, LocationStatus.GPS.name) ?: LocationStatus.GPS.name
        return LocationStatus.valueOf(locationStatus)
    }

    // Notification settings
    override fun setNotificationStatus(status: Boolean) {
        sharedPreferences.edit().putBoolean(Keys.NOTIFICATION_STATUS_KEY, status).apply()
    }

    override fun getNotificationStatus(): Boolean {
        return sharedPreferences.getBoolean(Keys.NOTIFICATION_STATUS_KEY, false)
    }

    override fun setLocation(latitude: Double, longitude: Double) {
            with(sharedPreferences.edit()) {
                putString(Keys.LATITUDE_KEY, latitude.toString())
                putString(Keys.LONGITUDE_KEY, longitude.toString())
                apply()
            }
        }


    override fun getLocation(): Pair<Double, Double>? {
        val latitude = sharedPreferences.getString(Keys.LATITUDE_KEY, null)
        val longitude = sharedPreferences.getString(Keys.LONGITUDE_KEY, null)
        return if (latitude != null  && longitude != null ) {
            Pair( parseDouble(latitude), parseDouble(longitude))
        } else {
            null
        }
    }

}

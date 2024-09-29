package com.example.weather.utils.model.repository

import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.local.shared_perefernces.ISharedPreferencesManager

class FakeSharedPreferencesManager : ISharedPreferencesManager {
    override fun setFirstLaunchCompleted(status: Boolean) {
        TODO("Not yet implemented")
    }

    override fun isFirstLaunch(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setLanguage(lang: Language) {
        TODO("Not yet implemented")
    }

    override fun getLanguage(): Language {
        return Language.ENGLISH
    }

    override fun setTemperatureUnit(unit: Temperature) {
        TODO("Not yet implemented")
    }

    override fun getTemperatureUnit(): Temperature {
        TODO("Not yet implemented")
    }

    override fun setWindSpeedUnit(unit: WindSpeed) {
        TODO("Not yet implemented")
    }

    override fun getWindSpeedUnit(): WindSpeed {
        TODO("Not yet implemented")
    }

    override fun setLocationStatus(locationStatus: LocationStatus) {
        TODO("Not yet implemented")
    }

    override fun getLocationStatus(): LocationStatus {
        TODO("Not yet implemented")
    }

    override fun setNotificationStatus(status: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getNotificationStatus(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setLocation(latitude: Double, longitude: Double) {
        TODO("Not yet implemented")
    }

    override fun getLocation(): Pair<Double, Double>? {
        TODO("Not yet implemented")
    }
}
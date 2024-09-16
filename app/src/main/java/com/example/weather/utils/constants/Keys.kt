package com.example.weather.utils.constants


object Keys {
    //Shared Preferences Keys
    const val SHARED_PREFERENCES_NAME = "WeatherAppPrefs"

    const val LANGUAGE_KEY = "lang"
    const val DEFAULT_LANGUAGE = "en"

    const val TEMPERATURE_UNIT_KEY = "temp_unit"
    const val DEFAULT_TEMPERATURE_UNIT = "metric"

    const val WIND_SPEED_UNIT_KEY = "wind_unit"
    const val DEFAULT_WIND_SPEED_UNIT = "m/s"

    const val LOCATION_KEY = "location"

    const val NOTIFICATION_STATUS_KEY = "notification_enabled"

    //API Keys
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/weather?"

}

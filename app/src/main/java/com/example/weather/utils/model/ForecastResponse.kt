package com.example.weather.utils.model

data class ForecastResponse(

    val cod: String,
    val message: Double,
    val cnt: Int,
    val list: List<DailyForecastItem>,
    val city: City
)

data class DailyForecastItem(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val speed: Double,
    val pressure: Int,
    val humidity: Int,
    val weather: List<Weather>,
    val temp: Temperature,
    val deg: Int,
    val gust: Double,
    val clouds: Int,
    val pop: Double,
    val rain: Double
)

data class Temperature(
   val day: Double,
   val min :Double,
   val max: Double,
   val night: Double,
   val eve: Double,
   val morn: Double
)
data class City(
    val id: Int,
    val name: String,
    val coord: Coordinates,
    val country: String,
    val population: Int,
    val timezone: Int,
)
/*
      "temp": {

      },
      "feels_like": {
        "day": 290.06,
        "night": 285.28,
        "eve": 286.36,
        "morn": 284.15
      },

      "weather": [
        {
          "id": 500,
          "main": "Rain",
          "description": "light rain",
          "icon": "10d"
        }
      ],

    },*/
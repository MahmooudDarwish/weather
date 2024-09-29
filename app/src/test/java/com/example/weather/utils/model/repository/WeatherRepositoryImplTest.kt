package com.example.weather.utils.model.repository

import com.example.weather.utils.local.room.local_data_source.FakeLocalDataSource
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSource
import com.example.weather.utils.local.shared_perefernces.FakeSharedPreferencesManager
import com.example.weather.utils.local.shared_perefernces.ISharedPreferencesManager
import com.example.weather.utils.model.API.DailyForecastItem
import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.API.HourlyWeatherResponse
import com.example.weather.utils.model.API.Temperature
import com.example.weather.utils.model.API.WeatherResponse
import com.example.weather.utils.model.City
import com.example.weather.utils.model.Clouds
import com.example.weather.utils.model.Coordinates
import com.example.weather.utils.model.ForecastItem
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.Main
import com.example.weather.utils.model.Weather
import com.example.weather.utils.model.Wind
import com.example.weather.utils.remote.FakeRemoteDataSource
import com.example.weather.utils.remote.WeatherRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WeatherRepositoryImplTest {

    val mockedHourlyWeatherResponse = mutableListOf(
        HourlyWeatherResponse(
            cod = "200",
            message = 0,
            cnt = 48,
            list = listOf(
                ForecastItem(
                    dt = 1633035600,
                    main = Main(temp = 28.5, pressure = 1012, humidity = 60),
                    weather = listOf(
                        Weather(description = "Clear sky", icon = "01d")
                    ),
                    clouds = Clouds(all = 0),
                    wind = Wind(speed = 5.0),
                    dt_txt = "2021-09-30 12:00:00"
                ),
                ForecastItem(
                    dt = 1633046400,
                    main = Main(temp = 27.0, pressure = 1013, humidity = 62),
                    weather = listOf(
                        Weather(description = "Few clouds", icon = "02d")
                    ),
                    clouds = Clouds(all = 10),
                    wind = Wind(speed = 4.5),
                    dt_txt = "2021-09-30 15:00:00"
                )
            ),
            city = City(coord = Coordinates(lon = 30.0, lat = 31.0))
        ),
        HourlyWeatherResponse(
            cod = "200",
            message = 0,
            cnt = 48,
            list = listOf(
                ForecastItem(
                    dt = 1633046400,
                    main = Main(temp = 24.5, pressure = 1011, humidity = 70),
                    weather = listOf(
                        Weather(description = "Partly cloudy", icon = "03d")
                    ),
                    clouds = Clouds(all = 20),
                    wind = Wind(speed = 4.0),
                    dt_txt = "2021-09-30 18:00:00"
                ),
                ForecastItem(
                    dt = 1633057200,
                    main = Main(temp = 22.0, pressure = 1010, humidity = 75),
                    weather = listOf(
                        Weather(description = "Rain", icon = "10d")
                    ),
                    clouds = Clouds(all = 50),
                    wind = Wind(speed = 3.5),
                    dt_txt = "2021-09-30 21:00:00"
                )
            ),
            city = City(coord = Coordinates(lon = 40.0, lat = 35.0))
        )
    )
    val mockedDailyWeatherResponse = mutableListOf(
        DailyWeatherResponse(
            cod = "200",
            message = 0.0,
            cnt = 7,
            list = listOf(
                DailyForecastItem(
                    dt = 1633035600,
                    speed = 4.5,
                    pressure = 1010,
                    humidity = 65,
                    weather = listOf(
                        Weather(description = "Rain", icon = "10d")
                    ),
                    temp = Temperature(min = 15.0, max = 22.0),
                    clouds = 40
                ),
                DailyForecastItem(
                    dt = 1633122000,
                    speed = 5.0,
                    pressure = 1011,
                    humidity = 60,
                    weather = listOf(
                        Weather(description = "Partly cloudy", icon = "03d")
                    ),
                    temp = Temperature(min = 16.0, max = 23.0),
                    clouds = 30
                )
            ),
            city = City(coord = Coordinates(lon = 30.0, lat = 31.0))
        ),
        DailyWeatherResponse(
            cod = "200",
            message = 0.0,
            cnt = 7,
            list = listOf(
                DailyForecastItem(
                    dt = 1633208400,
                    speed = 3.0,
                    pressure = 1013,
                    humidity = 55,
                    weather = listOf(
                        Weather(description = "Sunny", icon = "01d")
                    ),
                    temp = Temperature(min = 17.0, max = 25.0),
                    clouds = 0
                ),
                DailyForecastItem(
                    dt = 1633294800,
                    speed = 2.5,
                    pressure = 1014,
                    humidity = 50,
                    weather = listOf(
                        Weather(description = "Clear sky", icon = "01n")
                    ),
                    temp = Temperature(min = 18.0, max = 26.0),
                    clouds = 10
                )
            ),
            city = City(coord = Coordinates(lon = 40.0, lat = 35.0))
        )
    )
    val mockedWeatherResponse = mutableListOf(
        WeatherResponse(
            coord = Coordinates(lon = 30.0, lat = 31.0),
            weather = listOf(
                Weather(description = "Clear sky", icon = "01d")
            ),
            main = Main(temp = 28.5, pressure = 1012, humidity = 60),
            wind = Wind(speed = 5.0),
            clouds = Clouds(all = 0),
            dt = 1633035600,
            id = 123456,
            name = "CityName1"
        ),
        WeatherResponse(
            coord = Coordinates(lon = 40.0, lat = 35.0),
            weather = listOf(
                Weather(description = "Few clouds", icon = "02d")
            ),
            main = Main(temp = 22.0, pressure = 1015, humidity = 70),
            wind = Wind(speed = 3.5),
            clouds = Clouds(all = 20),
            dt = 1633039200,
            id = 789012,
            name = "CityName2"
        )
    )

    val mockedDailyWeatherEntities = mutableListOf(
        DailyWeatherEntity(
            longitude = 30.0,
            latitude = 31.0,
            dt = 1633035600,
            minTemp = 15.0,
            maxTemp = 22.0,
            windSpeed = 4.5,
            pressure = 1010,
            humidity = 65,
            description = "Rain",
            icon = "10d",
            clouds = 40,
            isFavorite = true
        ),
        DailyWeatherEntity(
            longitude = 40.0,
            latitude = 35.0,
            dt = 1633122000,
            minTemp = 16.0,
            maxTemp = 23.0,
            windSpeed = 5.0,
            pressure = 1011,
            humidity = 60,
            description = "Partly cloudy",
            icon = "03d",
            clouds = 30,
            isFavorite = false
        )
    )
    val mockedHourlyWeatherEntities = mutableListOf(
        HourlyWeatherEntity(
            longitude = 30.0,
            latitude = 31.0,
            dt = 1633035600,
            temp = 28.5,
            pressure = 1012,
            humidity = 60,
            windSpeed = 5.0,
            description = "Clear sky",
            icon = "01d",
            clouds = 0,
            dt_txt = "2021-09-30 12:00:00",
            isFavorite = true
        ),
        HourlyWeatherEntity(
            longitude = 40.0,
            latitude = 35.0,
            dt = 1633046400,
            temp = 27.0,
            pressure = 1013,
            humidity = 62,
            windSpeed = 4.5,
            description = "Few clouds",
            icon = "02d",
            clouds = 10,
            dt_txt = "2021-09-30 15:00:00",
            isFavorite = false
        )
    )
    val mockedWeatherEntities = mutableListOf(
        WeatherEntity(
            longitude = 30.0,
            latitude = 31.0,
            description = "Clear sky",
            icon = "01d",
            temp = 28.5,
            pressure = 1012,
            humidity = 60,
            windSpeed = 5.0,
            clouds = 0,
            dt = 1633035600,
            name = "CityName1",
            isFavorite = true
        ),
        WeatherEntity(
            longitude = 40.0,
            latitude = 35.0,
            description = "Few clouds",
            icon = "02d",
            temp = 27.0,
            pressure = 1015,
            humidity = 70,
            windSpeed = 3.5,
            clouds = 20,
            dt = 1633039200,
            name = "CityName2",
            isFavorite = false
        )
    )


    lateinit var remoteDataSource: WeatherRemoteDataSource
    lateinit var localDataSource: WeatherLocalDataSource
    lateinit var sharedPreferencesManager: ISharedPreferencesManager
    lateinit var repository : WeatherRepository
    @Before
    fun createRepository() {
        remoteDataSource = FakeRemoteDataSource(mockedHourlyWeatherResponse , mockedDailyWeatherResponse, mockedWeatherResponse)
        localDataSource = FakeLocalDataSource(mockedWeatherEntities, mockedHourlyWeatherEntities, mockedDailyWeatherEntities)
        sharedPreferencesManager = FakeSharedPreferencesManager()
        repository = WeatherRepositoryImpl.getInstance(remoteDataSource, localDataSource, sharedPreferencesManager)
    }

    ///API
    //Get weather method
    @Test
    fun getWeather_validCoordinates_returnsWeatherData() = runTest {
        //given  valid coordinates
        val latitude = 31.0
        val longitude = 30.0

        //When
        val weatherFlow = repository.fetchWeatherData(latitude = latitude, longitude =  longitude)
        val weather = weatherFlow.first()

        //Then
        assertEquals(mockedWeatherResponse[0], weather)
    }

    //latitude ranging from -90.0000000 to 90.0000000 and longitude ranging from -180.0000000 to 180.0000000
    @Test
    fun getWeather_invalidCoordinates_returnsNull() = runTest {
        //Give out limit coordinates
        val invalidLatitude = -91.0
        val invalidLongitude = 181.0
        //When
        val weatherFlow = repository.fetchWeatherData(latitude = invalidLatitude, longitude =  invalidLongitude)
        val weather = weatherFlow.first()

        //Then
        assertEquals(weather, null)
    }

    ///ROOM
    //Insert daily weather
    @Test
    fun insertDailyWeather_validData_insertsCorrectly() = runTest {
        // Given a new daily weather entity
        val newWeatherList = listOf(
            DailyWeatherEntity(
                longitude = 50.0,
                latitude = 60.0,
                dt = 1633208400,
                minTemp = 12.0,
                maxTemp = 20.0,
                windSpeed = 3.0,
                pressure = 1000,
                humidity = 50,
                description = "Cloudy",
                icon = "03d",
                clouds = 20,
                isFavorite = true
            )
        )

        // When inserting the new daily weather
        repository.insertDailyWeather(newWeatherList)

        // Then verify the new weather is inserted
        val result = repository.getDailyWeather(lon = 50.0, lat = 60.0).first()
        assertEquals(newWeatherList, result)
    }

    @Test
    fun insertDailyWeather_multipleData_insertsAllCorrectly() = runTest {
        // Given multiple daily weather entities
        val newWeatherList = listOf(
            DailyWeatherEntity(
                longitude = 50.0,
                latitude = 60.0,
                dt = 1633208400,
                minTemp = 12.0,
                maxTemp = 20.0,
                windSpeed = 3.0,
                pressure = 1000,
                humidity = 50,
                description = "Cloudy",
                icon = "03d",
                clouds = 20,
                isFavorite = true
            ),
            DailyWeatherEntity(
                longitude = 50.0,
                latitude = 60.0,
                dt = 1633208401,
                minTemp = 10.0,
                maxTemp = 18.0,
                windSpeed = 2.5,
                pressure = 1005,
                humidity = 55,
                description = "Rainy",
                icon = "10d",
                clouds = 30,
                isFavorite = false
            )
        )

        // When inserting the new daily weather
        repository.insertDailyWeather(newWeatherList)

        // Then verify all weather entities are inserted
        val result = repository.getDailyWeather(lon = 50.0, lat = 60.0).first()
        assertEquals(newWeatherList, result)
    }
    @Test
    fun insertDailyWeather_duplicateData_insertsCorrectly() = runTest {
        // Given a daily weather entity
        val initialSize = mockedDailyWeatherEntities.size

        val duplicateWeatherList = listOf(
            DailyWeatherEntity(
                longitude = 50.0,
                latitude = 60.0,
                dt = 1633208400,
                minTemp = 12.0,
                maxTemp = 20.0,
                windSpeed = 3.0,
                pressure = 1000,
                humidity = 50,
                description = "Cloudy",
                icon = "03d",
                clouds = 20,
                isFavorite = true
            )
        )

        // When inserting the same daily weather twice
        repository.insertDailyWeather(duplicateWeatherList)
        repository.insertDailyWeather(duplicateWeatherList)



        // Then verify it is still inserted once
        val newSize = mockedDailyWeatherEntities.size
        assertEquals(initialSize + 1, newSize)
    }


    //Delete daily weather
    @Test
    fun deleteDailyWeather_existingData_removesCorrectly() = runTest {
        // Given existing daily weather entities
        val initialSize = mockedDailyWeatherEntities.size

        // When deleting a daily weather entity
        repository.deleteFavoriteDailyWeather(lon = 30.0, lat = 31.0)

        // Then verify that the size is reduced by one
        val result = mockedDailyWeatherEntities.size
        assertEquals(initialSize - 1, result)

        // And ensure the specific entity no longer exists
        val filteredListAfterDelete = mockedDailyWeatherEntities.filter { it.longitude == 30.0 && it.latitude == 31.0 }
        assertEquals(filteredListAfterDelete.isEmpty() , true)
    }
    @Test
    fun deleteDailyWeather_nonExistingData_doesNotChangeSize() = runTest {
        // Given existing daily weather entities
        val initialWeather = DailyWeatherEntity(
            longitude = 30.0,
            latitude = 31.0,
            dt = 1633208400,
            minTemp = 12.0,
            maxTemp = 20.0,
            windSpeed = 3.0,
            pressure = 1000,
            humidity = 50,
            description = "Cloudy",
            icon = "03d",
            clouds = 20,
            isFavorite = true
        )

        repository.insertDailyWeather(listOf(initialWeather))
        val initialSize = mockedDailyWeatherEntities.size

        // When trying to delete a non-existing entity
        repository.deleteFavoriteDailyWeather(lon = 99.0, lat = 99.0)

        // Then verify that the size remains unchanged
        val result = mockedDailyWeatherEntities.size
        assertEquals(initialSize, result)
    }
    @Test
    fun deleteDailyWeather_multipleEntries_deletesCorrectly() = runTest {
        // Given multiple daily weather entities with the same location
        val weatherEntities = listOf(
            DailyWeatherEntity(longitude = 30.0, latitude = 31.0, dt = 1633208400, minTemp = 12.0, maxTemp = 20.0, windSpeed = 3.0, pressure = 1000, humidity = 50, description = "Cloudy", icon = "03d", clouds = 20, isFavorite = true),
            DailyWeatherEntity(longitude = 30.0, latitude = 31.0, dt = 1633208401, minTemp = 12.0, maxTemp = 22.0, windSpeed = 3.5, pressure = 1000, humidity = 60, description = "Partly Cloudy", icon = "02d", clouds = 15, isFavorite = true)
        )

        repository.insertDailyWeather(weatherEntities)
        val initialSize = mockedDailyWeatherEntities.size

        // When deleting one of some location's entities
        repository.deleteFavoriteDailyWeather(lon = 30.0, lat = 31.0) // Delete the first entity also from the initial list

        // Then verify that the size is reduced by one
        val result = mockedDailyWeatherEntities.size
        assertEquals(initialSize-3, result) //Should be 1 as 4-3
    }




}
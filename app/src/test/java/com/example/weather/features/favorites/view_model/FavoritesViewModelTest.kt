package com.example.weather.features.favorites.view_model

import com.example.weather.utils.model.DataState
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.FakeWeatherRepositoryImp
import com.example.weather.utils.model.repository.WeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class FavoritesViewModelTest{
    lateinit var repository : WeatherRepository
    lateinit var viewModel: FavoritesViewModel



    @Before
    fun createRepository() {
        repository = FakeWeatherRepositoryImp()
        viewModel = FavoritesViewModel(repository)
    }

    // Test case for fetchAllFavoriteWeather
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetchAllFavoriteWeather_favoritesExist_favoritesAreFetched() = runTest {
        // Given: Add some favorite weather entities with complete properties
        val favoriteWeather1 = WeatherEntity(
            longitude = 1.0,
            latitude = 2.0,
            description = "Favorite 1",
            icon = "icon1",
            temp = 20.0,
            pressure = 1013,
            humidity = 50,
            windSpeed = 5.0,
            clouds = 10,
            dt = 1234567890,
            name = "Location 1",
            isFavorite = true
        )

        val favoriteWeather2 = WeatherEntity(
            longitude = 3.0,
            latitude = 4.0,
            description = "Favorite 2",
            icon = "icon2",
            temp = 22.0,
            pressure = 1012,
            humidity = 55,
            windSpeed = 6.0,
            clouds = 15,
            dt = 1234567891,
            name = "Location 2",
            isFavorite = true
        )

        repository.insertWeather(favoriteWeather1)
        repository.insertWeather(favoriteWeather2)

        // When: Fetch all favorite weather
        viewModel.fetchAllFavoriteWeather()
        advanceUntilIdle()


        // Then: Check if the fetched favorites match the expected favorites
        assertEquals(DataState.Success(listOf(favoriteWeather1, favoriteWeather2)), viewModel.favorites.value)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetchAllFavoriteWeather_noFavorites_returnsEmptyList() = runTest {
        // When: Fetch all favorite weather
        viewModel.fetchAllFavoriteWeather()
        advanceUntilIdle()

        // Then: Check if the fetched favorites are empty
        assertEquals(DataState.Success(emptyList<WeatherEntity>()), viewModel.favorites.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetchAllFavoriteWeather_duplicatesExist_favoritesAreFetchedOnce() = runTest {
        // Given: Add some favorite weather entities with duplicate entries
        val favoriteWeather = WeatherEntity(
            longitude = 1.0,
            latitude = 2.0,
            description = "Favorite Duplicate",
            icon = "iconDuplicate",
            temp = 21.0,
            pressure = 1014,
            humidity = 60,
            windSpeed = 5.5,
            clouds = 12,
            dt = 1234567892,
            name = "Duplicate Location",
            isFavorite = true
        )

        repository.insertWeather(favoriteWeather)
        repository.insertWeather(favoriteWeather) // Duplicate
        repository.insertWeather(favoriteWeather) // Duplicate

        // When: Fetch all favorite weather
        viewModel.fetchAllFavoriteWeather()
        advanceUntilIdle()

        // Then: Check if the fetched favorites only contain one instance of the duplicate
        assertEquals(DataState.Success(listOf(favoriteWeather)), viewModel.favorites.value)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun fetchAllFavoriteWeather_afterDeletion_favoritesAreFetchedCorrectly() = runTest {
        // Given: Add two favorite weather entities
        val favoriteWeather1 = WeatherEntity(
            longitude = 1.0,
            latitude = 2.0,
            description = "Favorite 1",
            icon = "icon1",
            temp = 20.0,
            pressure = 1013,
            humidity = 50,
            windSpeed = 5.0,
            clouds = 10,
            dt = 1234567890,
            name = "Location 1",
            isFavorite = true
        )

        val favoriteWeather2 = WeatherEntity(
            longitude = 3.0,
            latitude = 4.0,
            description = "Favorite 2",
            icon = "icon2",
            temp = 22.0,
            pressure = 1012,
            humidity = 55,
            windSpeed = 6.0,
            clouds = 15,
            dt = 1234567891,
            name = "Location 2",
            isFavorite = true
        )

        repository.insertWeather(favoriteWeather1)
        repository.insertWeather(favoriteWeather2)

        // When: Delete the first favorite weather
        viewModel.deleteFavorite(favoriteWeather1)
        advanceUntilIdle()

        // Then: Fetch all favorites and check if only the remaining favorite is present
        viewModel.fetchAllFavoriteWeather()
        advanceUntilIdle()

        assertEquals(DataState.Success(listOf(favoriteWeather2)), viewModel.favorites.value)
    }



    // Test case for deleteFavorite
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteFavorite_existingFavorite_favoriteIsRemoved() = runTest {
        // Given: Add a favorite weather entity with complete properties
        val favoriteWeather = WeatherEntity(
            longitude = 1.0,
            latitude = 2.0,
            description = "Favorite",
            icon = "icon",
            temp = 20.0,
            pressure = 1013,
            humidity = 50,
            windSpeed = 5.0,
            clouds = 10,
            dt = 1234567890,
            name = "Location",
            isFavorite = true
        )
        repository.insertWeather(favoriteWeather)

        // When: Delete the favorite weather
        viewModel.deleteFavorite(favoriteWeather)
        advanceUntilIdle()

        // Then: Fetch all favorites and check if the deleted favorite is gone
        viewModel.fetchAllFavoriteWeather()
        advanceUntilIdle()
        assertEquals(DataState.Success(emptyList<WeatherEntity>()), viewModel.favorites.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteFavorite_nonExistingFavorite_noErrorThrown() = runTest {
        // Given: A weather entity that does not exist in the favorites
        val nonExistingWeather = WeatherEntity(
            longitude = 5.0,
            latitude = 6.0,
            description = "Non-Existing",
            icon = "icon",
            temp = 25.0,
            pressure = 1015,
            humidity = 60,
            windSpeed = 4.0,
            clouds = 20,
            dt = 1234567892,
            name = "Unknown Location",
            isFavorite = false
        )

        // When: Attempt to delete a non-existing favorite
        viewModel.deleteFavorite(nonExistingWeather)
        advanceUntilIdle()

        // Then: Check if the favorites are still empty (or remain unchanged)
        viewModel.fetchAllFavoriteWeather()
        advanceUntilIdle()

        assertEquals(DataState.Success(emptyList<WeatherEntity>()), viewModel.favorites.value)
    }



}
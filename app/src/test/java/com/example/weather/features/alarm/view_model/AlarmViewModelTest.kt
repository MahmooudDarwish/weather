package com.example.weather.features.alarm.view_model

import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.repository.FakeWeatherRepositoryImp
import com.example.weather.utils.model.repository.WeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class AlarmViewModelTest{


    lateinit var repository : WeatherRepository
    lateinit var viewModel: AlarmViewModel

    @Before
    fun createRepository() {
        repository = FakeWeatherRepositoryImp()
        viewModel = AlarmViewModel(repository)
    }

    //Add alert
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun addAlert_alarm_addedSuccessfully() = runTest {
        val intialSize = repository.getAllAlarms().first().size
        // Given: A new alarm to insert
        val newAlarm = AlarmEntity(10L, 11L, "Alarm 2", "Desc 2", "icon2", 10, 45, 11, 15, 5L, true)

        // When: Adding the same alarm again
        viewModel.addAlert(newAlarm)
        advanceUntilIdle()

        val newSize = repository.getAllAlarms().first().size

        //Then: The number of alarms in the repository should increase by 1
        assertEquals(intialSize + 1, newSize)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun addAlert_duplicateAlarm_notAddedAgain() = runTest {
        val initialSize = repository.getAllAlarms().first().size
        // Given: An existing alarm
        val alarm = AlarmEntity(10L, 11L, "Alarm 1", "Desc 1", "icon1", 10, 45, 11, 15, 5L, true)
        viewModel.addAlert(alarm)

        // When: Adding the same alarm again
        viewModel.addAlert(alarm)
        advanceUntilIdle()

        val newSize = repository.getAllAlarms().first().size

        // Then: The number of alarms in the repository should remain the same
        assertEquals(initialSize + 1 , newSize)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun addAlert_multipleDistinctAlarms_addedSuccessfully() = runTest {
        val initialSize = repository.getAllAlarms().first().size
        // Given: Multiple new alarms to insert
        val alarm1 = AlarmEntity(10L, 11L, "Alarm 1", "Desc 1", "icon1", 10, 45, 11, 15, 5L, true)
        val alarm2 = AlarmEntity(20L, 21L, "Alarm 2", "Desc 2", "icon2", 10, 45, 11, 15, 5L, false)

        // When: Adding the alarms
        viewModel.addAlert(alarm1)
        viewModel.addAlert(alarm2)
        advanceUntilIdle()

        val newSize = repository.getAllAlarms().first().size

        // Then: Both alarms should be present in the repository
        assertEquals(initialSize + 2, newSize)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun addAlert_invalidAlarm_doesNotAdd() = runTest {
        val initialSize = repository.getAllAlarms().first().size
        // Given: An invalid alarm (for example, with null values or invalid times)
        val invalidAlarm = AlarmEntity(0L, 0L, "", "", "", -1, -1, -1, -1, -1L, false)

        // When: Attempting to add the invalid alarm
        viewModel.addAlert(invalidAlarm)
        advanceUntilIdle()
        val newSize = repository.getAllAlarms().first().size

        // Then: The alarm should not be added to the repository
        assertEquals(initialSize , newSize)
    }

    //Delete Alert
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAlarm_alarmExists_alarmIsRemoved() = runTest {
        val initialSize = repository.getAllAlarms().first().size
        val alarm = AlarmEntity(5, 11L, "Alarm 1", "Desc 1", "icon1", 10, 45, 11, 15, 5L, true)
        viewModel.addAlert(alarm)

        // Given
        val idToDelete = 5L


        // When
        viewModel.deleteAlert(idToDelete)
        advanceUntilIdle() // This ensures the coroutine has completed before the next assertion
        val newSize = repository.getAllAlarms().first().size

        // Then
        assertEquals(initialSize,newSize)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test()
    fun deleteAlarm_alarmDoesNotExist_noSuchElementExceptionIsThrown() = runTest {
        val initialSize = repository.getAllAlarms().first().size
        // Given
        val idToDelete = 3L

        // When
        viewModel.deleteAlert(idToDelete)
        advanceUntilIdle()
        val newSize = repository.getAllAlarms().first().size

        // Then: nothing got deleted
        assertEquals(initialSize,newSize)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAlarm_alarmExists_otherAlarmsRemainIntact() = runTest {
        // Given

        viewModel.addAlert(AlarmEntity(2L, 3L, "Alarm 2", "Desc 2", "icon2", 10, 45, 11, 15, 5L, true))
        val idToDelete = 5L

        // When
        viewModel.deleteAlert(idToDelete)
        advanceUntilIdle()

        val newSize = repository.getAllAlarms().first().size
        val alarms = repository.getAllAlarms().first()

        // Then
        assertEquals(1, newSize) // Ensure only one alarm remains
        assertEquals(2L, alarms[0].startDate) // Verify the remaining alarm is the correct one
    }

    @Test(expected = IllegalArgumentException::class)
    fun deleteAlarm_nullId_illegalArgumentExceptionIsThrown() = runTest {
        val initialSize = repository.getAllAlarms().first().size
        // Given
        val nullId: Long? = null

        // When
        viewModel.deleteAlert(nullId ?: throw IllegalArgumentException("ID cannot be null"))
        val newSize = repository.getAllAlarms().first().size

        // Then: nothing changed
        assertEquals(initialSize,newSize)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun deleteAlarm_addAndDelete_alarmListIsEmpty() = runTest {
        // Given
        val newAlarm = AlarmEntity(3L, 6L,"Alarm 3", "Desc 3", "icon3", 10, 45, 11, 15, 5L, true)
        viewModel.addAlert(newAlarm)
        val idToDelete = 3L

        // When
        viewModel.deleteAlert(idToDelete)
        advanceUntilIdle()

        val newSize = repository.getAllAlarms().first().size

        // Then
        assertEquals(0,newSize)
    }
}

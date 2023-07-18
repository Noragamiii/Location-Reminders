package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.nullValue
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var reminderDatabase: RemindersDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        reminderDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository =
            RemindersLocalRepository(reminderDatabase.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() {
        reminderDatabase.close()
    }

    @Test
    fun remindersLocalRepository_save() = runBlocking {

        // Create data
        val reminderDataItem = ReminderDTO(
            title = "Title",
            "Description",
            "Location",
            27.0,
            0.5
        )

        // Save data
        remindersLocalRepository.saveReminder(reminderDataItem)

        // Get data
        val getData = remindersLocalRepository.getReminder(reminderDataItem.id)
        getData as Result.Success

        // Verify
        assertThat(getData, not(nullValue()))
        assertThat(getData.data.id, `is`(reminderDataItem.id))
        assertThat(getData.data.title, `is`(reminderDataItem.title))
        assertThat(getData.data.description, `is`(reminderDataItem.description))
        assertThat(getData.data.location, `is`(reminderDataItem.location))
        assertThat(getData.data.latitude, `is`(reminderDataItem.latitude))
        assertThat(getData.data.longitude, `is`(reminderDataItem.longitude))
    }

    @Test
    fun remindersLocalRepository_save_list() = runBlocking {

        // Create data
        val reminderDataItem1 = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            27.0,
            0.5
        )

        val reminderDataItem2 = ReminderDTO(
            "Title2",
            "Description2",
            "Location2",
            27.0,
            0.5
        )

        val reminderDataItem3 = ReminderDTO(
            "Title3",
            "Description3",
            "Location3",
            27.0,
            0.5
        )

        // Save data
        remindersLocalRepository.saveReminder(reminderDataItem1)
        remindersLocalRepository.saveReminder(reminderDataItem2)
        remindersLocalRepository.saveReminder(reminderDataItem3)

        // Get data
        val getData = remindersLocalRepository.getReminders()
        getData as Result.Success

        // Verify
        assertThat(getData.data.size, `is`(3))
        assertThat(getData.data[0].title, `is`(reminderDataItem1.title))
        assertThat(getData.data[1].title, `is`(reminderDataItem2.title))
        assertThat(getData.data[2].title, `is`(reminderDataItem3.title))
    }

    @Test
    fun remindersLocalRepository_deleteAll() = runBlocking {

        // Create data
        val reminderDataItem1 = ReminderDTO(
            "Title1",
            "Description1",
            "Location1",
            27.0,
            0.5
        )

        val reminderDataItem2 = ReminderDTO(
            "Title2",
            "Description2",
            "Location2",
            27.0,
            0.5
        )

        val reminderDataItem3 = ReminderDTO(
            "Title3",
            "Description3",
            "Location3",
            27.0,
            0.5
        )

        // Save data
        remindersLocalRepository.saveReminder(reminderDataItem1)
        remindersLocalRepository.saveReminder(reminderDataItem2)
        remindersLocalRepository.saveReminder(reminderDataItem3)

        // Get data
        val getData = remindersLocalRepository.getReminders()
        getData as Result.Success

        // Check size
        assertThat(getData.data.size, `is`(3))

        // Delete all reminders
        remindersLocalRepository.deleteAllReminders()

        // Check size
        val result = remindersLocalRepository.getReminders()
        result as Result.Success
        assertThat(result.data.size, `is`(0))
    }

    @Test
    fun remindersLocalRepository_returnError() = runBlocking {

        // Load data
        val result = remindersLocalRepository.getReminder("27")
        result as Result.Error

        // Verify
        assertEquals("Reminder not found!", result.message)
    }

}
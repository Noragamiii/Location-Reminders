package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.core.IsNot
import org.junit.After
import org.junit.Test
import org.koin.core.component.getScopeId

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

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
    }

    @After
    fun closeDb() {
        reminderDatabase.close()
    }

    @Test
    fun reminderDAO_save() = runBlocking {

        // Create data
        val reminderDataItem = ReminderDTO(
            title = "Title",
            "Description",
            "Location",
            27.0,
            0.5
        )

        // Save data
        reminderDatabase.reminderDao().saveReminder(reminderDataItem)

        // Get data
        val getData = reminderDatabase.reminderDao().getReminderById(reminderDataItem.id)

        // Verify
        assertThat(getData, IsNot.not(Matchers.nullValue()))
        assertThat(getData?.id, `is`(reminderDataItem.id))
        assertThat(getData?.title, `is`(reminderDataItem.title))
        assertThat(getData?.description, `is`(reminderDataItem.description))
        assertThat(getData?.location, `is`(reminderDataItem.location))
        assertThat(getData?.latitude, `is`(reminderDataItem.latitude))
        assertThat(getData?.longitude, `is`(reminderDataItem.longitude))
    }

    @Test
    fun reminderDAO_save_list() = runBlocking {

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
        reminderDatabase.reminderDao().saveReminder(reminderDataItem1)
        reminderDatabase.reminderDao().saveReminder(reminderDataItem2)
        reminderDatabase.reminderDao().saveReminder(reminderDataItem3)

        // Get data
        val getData = reminderDatabase.reminderDao().getReminders()

        // Verify
        assertThat(getData?.size, `is`(3))
        assertThat(getData[0]?.description, `is`(reminderDataItem1.description))
        assertThat(getData[1]?.description, `is`(reminderDataItem2.description))
        assertThat(getData[2]?.description, `is`(reminderDataItem3.description))
    }

    @Test
    fun reminderDAO_deleteAll() = runBlocking {

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
        reminderDatabase.reminderDao().saveReminder(reminderDataItem1)
        reminderDatabase.reminderDao().saveReminder(reminderDataItem2)
        reminderDatabase.reminderDao().saveReminder(reminderDataItem3)

        // Get data
        val getData = reminderDatabase.reminderDao().getReminders()

        // Check size
        assertThat(getData?.size, `is`(3))

        // Delete reminder
        reminderDatabase.reminderDao().deleteAllReminders()

        // Get data
        val result = reminderDatabase.reminderDao().getReminders()

        // Check size
        assertThat(result?.size, `is`(0))
    }

}
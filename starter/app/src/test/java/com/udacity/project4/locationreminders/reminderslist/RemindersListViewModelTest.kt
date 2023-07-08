package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Subject under test
    private lateinit var reminderListViewModel: RemindersListViewModel

    //Use a fake repository to be injected into the view-model
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_showLoading() = mainCoroutineRule.runBlockingTest {
        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Save reminder
        reminderListViewModel.loadReminders()

        // Then progress indicator is shown
        assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(true)
        )

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden
        assertThat(
            reminderListViewModel.showLoading.getOrAwaitValue(),
            CoreMatchers.`is`(false)
        )
    }

    @Test
    fun loadReminders_resultError() = mainCoroutineRule.runBlockingTest {

        // set error
        fakeDataSource.setReturnError(true)

        // Load reminders
        reminderListViewModel.loadReminders()

        // Show error message
        assertThat(
            reminderListViewModel.showSnackBar.getOrAwaitValue(),
            `is`("Test exception")
        )
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_deleteData() = mainCoroutineRule.runBlockingTest {

        // delete error
        fakeDataSource.deleteAllReminders()

        // Load reminders
        reminderListViewModel.loadReminders()

        // Check show data
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_ok() = mainCoroutineRule.runBlockingTest {
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

        // add data
        fakeDataSource.saveReminder(reminderDataItem1)
        fakeDataSource.saveReminder(reminderDataItem2)
        fakeDataSource.saveReminder(reminderDataItem3)

        // load data
        reminderListViewModel.loadReminders()
        val loadedItems = reminderListViewModel.remindersList.getOrAwaitValue()

        assertThat( (loadedItems.size), `is`(3))
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(false))
        assertThat( reminderListViewModel.remindersList.getOrAwaitValue(), (not(emptyList())))
        assertThat(loadedItems[0].title, `is`(reminderDataItem1.title))
        assertThat(loadedItems[1].title, `is`(reminderDataItem2.title))
        assertThat(loadedItems[2].title, `is`(reminderDataItem3.title))
    }
}
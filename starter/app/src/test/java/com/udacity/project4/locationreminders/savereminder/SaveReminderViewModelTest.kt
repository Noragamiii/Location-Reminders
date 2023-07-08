package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.junit.*
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config
import org.hamcrest.MatcherAssert.assertThat
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    //Use a fake repository to be injected into the view-model
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        saveReminderViewModel.onClear()
        stopKoin()
    }

    @Test
    fun saveReminder_title() = mainCoroutineRule.runBlockingTest {
        // Create data
        val reminderDataItem = ReminderDataItem(
            title = "",
            "Description",
            "Location",
            27.0,
            0.5
        )

        // Validate data
        var actual = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Check showSnack bar
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title))
        assertThat(actual, `is`(false))
    }

    @Test
    fun saveReminder_location() = mainCoroutineRule.runBlockingTest {
        // Create data
        val reminderDataItem = ReminderDataItem(
            title = "Title",
            "Description",
            "",
            27.0,
            0.5
        )

        // Validate data
        var actual = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // Check showSnack bar
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location))
        assertThat(actual, `is`(false))
    }

    @Test
    fun saveData_showLoading() = mainCoroutineRule.runBlockingTest {

        // Create data
        val reminderDataItem = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            27.0,
            0.5
        )

        // Pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // Save reminder
        saveReminderViewModel.saveReminder(reminderDataItem)

        // Then progress indicator is shown
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun saveData_success() = mainCoroutineRule.runBlockingTest {
        // Create data
        val reminderDataItem = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            27.0,
            0.5
        )

        // Save reminder
        saveReminderViewModel.saveReminder(reminderDataItem)

        // Check Toast
        assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !")
        )

        // Check Back
        assertEquals(saveReminderViewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }

    @Test
    fun validatedata_ok() = mainCoroutineRule.runBlockingTest {

        // Create data
        val reminderDataItem = ReminderDataItem(
            "Title",
            "Description",
            "Location",
            27.0,
            0.5
        )
        // validate
        val actual = saveReminderViewModel.validateEnteredData(reminderDataItem)

        // result
        assertThat(actual, `is`(true))
    }
}
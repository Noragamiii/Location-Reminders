package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.*
import org.junit.runner.manipulation.Ordering.Context
import org.koin.core.context.GlobalContext.stopKoin

@ExperimentalCoroutinesApi
class SaveReminderViewModelTest {

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Subject under test
    private lateinit var viewModel: SaveReminderViewModel

    //Use a fake repository to be injected into the view-model
    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var app: Application


    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext<android.content.Context>(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun saveReminder_ShowLoading() = runBlockingTest {
        // Pause dispatcher so you can verify initial values.
        val reminderDataItem = ReminderDataItem(
            "Title 100",
            "Description 100",
            "Location 100",
            37.8,
            -122.2
        )

        mainCoroutineRule.pauseDispatcher()

        // WHEN save reminder
        viewModel.saveReminder(reminderDataItem)

        // THEN: the progress indicator is shown.
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        // THEN: the progress indicator is hidden.
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

}
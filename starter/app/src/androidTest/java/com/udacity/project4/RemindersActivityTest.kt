package com.udacity.project4

import android.app.Application
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    KoinTest {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    //https://developer.android.com/reference/android/view/Window#getDecorView()
    private lateinit var decorView: View


    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(RemindersActivity::class.java)

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        activityScenarioRule.scenario.onActivity { activity ->
            decorView = activity.window.decorView
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun reminderActivity_addReminder() {

        // Start up Tasks screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click add
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Add content
        onView(withId(R.id.reminderTitle)).perform(typeText("Title 1"))
        onView(withId(R.id.reminderTitle)).check(
            ViewAssertions.matches(
                ViewMatchers.withText(
                    "Title 1"
                )
            )
        )
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))
        onView(withId(R.id.reminderDescription)).check(
            ViewAssertions.matches(
                ViewMatchers.withText(
                    "Description"
                )
            )
        )
        closeSoftKeyboard()

        // Select location
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())

        // Save
        onView(withId(R.id.save_location_button)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        // Check Toast
        // Reference https://stackoverflow.com/questions/28390574/checking-toast-message-in-android-espresso/28606603#28606603
        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(CoreMatchers.not(decorView))
        ).check(matches(isDisplayed()))

        // Check data
        onView(withText("Title 1")).check(matches(isDisplayed()))
        onView(withText("Description")).check(matches(isDisplayed()))
        onView(withText(R.string.title_activity_maps)).check(matches(isDisplayed()))
        activityScenario.close()
    }

    @Test
    fun addReminder_noLocation() {

        // Start up Tasks screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click add new task
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Add content
        onView(withId(R.id.reminderTitle)).perform(typeText("Title 1"))
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))
        closeSoftKeyboard()

        // Save reminder
        onView(withId(R.id.saveReminder)).perform(click())

        // show error
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

    @Test
    fun addReminder_noTitle() {

        // Start up Tasks screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click add new task
        onView(withId(R.id.addReminderFAB)).perform(click())

        // Add content
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"))
        closeSoftKeyboard()

        // Select location
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.map)).perform(longClick())

        // Save
        onView(withId(R.id.save_location_button)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        // show error
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()
    }

}

package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.*
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.*
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.*
import org.koin.dsl.module
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class SaveReminderFragmentTest {

    @get:Rule
    var executorRule = InstantTaskExecutorRule()
    private lateinit var viewModel: SaveReminderViewModel

    @Before
    fun initRepository() {
        stopKoin()

        val m = module {
            viewModel {
                SaveReminderViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }

            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(
                    ApplicationProvider.getApplicationContext()
                )
            }
        }

        startKoin {
            androidContext(
                ApplicationProvider.getApplicationContext()
            )
            modules(
                listOf(m)
            )
        }
        viewModel = GlobalContext.get().koin.get()
    }


    @Test
    fun validate_entered_title() {
        val navController = Mockito.mock(NavController::class.java)
        val scenarioFragment = launchFragmentInContainer<SaveReminderFragment>(
            Bundle.EMPTY,
            R.style.AppTheme
        )

        scenarioFragment.onFragment {
            Navigation.setViewNavController(
                it.view!!,
                navController
            )
        }

        onView(
            withId(R.id.saveReminder)
        ).perform(ViewActions.click())

        onView(
            withId(R.id.snackbar_text)
        ).check(
            matches(withText(R.string.err_enter_title))
        )
    }

    @Test
    fun validate_save_reminder_succeeds() {
        //with
        val title = "title"
        val description = "description"
        val location = "location"
        val reminderDataItem = ReminderDataItem(
            title, description, location,
            19.427010478706173, -99.16761544387603
        )

        val navController = Mockito.mock(NavController::class.java)
        val scenarioFragment = launchFragmentInContainer<SaveReminderFragment>(
            Bundle.EMPTY,
            R.style.AppTheme
        )

        scenarioFragment.onFragment {
            Navigation.setViewNavController(
                it.view!!,
                navController
            )
        }

        onView(
            withId(R.id.reminderTitle)
        ).perform(ViewActions.typeText(reminderDataItem.title))

        onView(
            withId(R.id.reminderDescription)
        ).perform(
            ViewActions.typeText(reminderDataItem.description)
        )

        viewModel.saveReminder(reminderDataItem)

        val textSuccess = "Reminder Saved !"
        assertThat(viewModel.showToast.getOrAwaitValue(), `is`(textSuccess))
    }
}
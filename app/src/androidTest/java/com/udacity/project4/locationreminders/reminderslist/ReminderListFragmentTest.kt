package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import org.koin.test.get
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest
    : AutoCloseKoinTest() {

//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    @get:Rule
    var instantTask = InstantTaskExecutorRule()
    private lateinit var context: Application
    private lateinit var repo: ReminderDataSource

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @Before
    fun init() = runBlocking {

        stopKoin()//stop the original app koin

        context = getApplicationContext()

        val m = module {
            viewModel {
                RemindersListViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }

            single {
                RemindersLocalRepository(get()) as ReminderDataSource
            }
            single {
                LocalDB.createRemindersDao(context)
            }
        }

        startKoin {
            modules(listOf(m))
        }


        repo = get()

        runBlocking {
            repo.deleteAll()
        }
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    @Test
    fun reminderList_displayUi() {
        launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )

        val viewInteraction = onView(
            withId(R.id.reminderssRecyclerView)
        )
        viewInteraction.check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )
    }

    @Test
    fun clickAnnetButton_goToSaveReminderFragment() {

        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )

        val controller = mock(NavController::class.java)

        fragmentScenario.onFragment {
            it.view?.let { view ->
                Navigation.setViewNavController(view, controller)
            }
        }

        val viewInteraction = onView(
            withId(R.id.addReminderFAB)
        )
        viewInteraction.perform(
            click()
        )

        val checker = verify(controller)
        checker.navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @Test
    fun noData_showNoData() {
        runBlocking {
            repo.deleteAll()

            launchFragmentInContainer<ReminderListFragment>(
                Bundle(),
                R.style.AppTheme
            )

            var viewInteraction = onView(withId(R.id.noDataTextView))
            viewInteraction.check(
                ViewAssertions.matches(ViewMatchers.isDisplayed())
            )

            viewInteraction = onView(withId(R.id.noDataTextView))
            viewInteraction.check(
                ViewAssertions.matches(ViewMatchers.withText(R.string.no_data))
            )
        }
    }
}
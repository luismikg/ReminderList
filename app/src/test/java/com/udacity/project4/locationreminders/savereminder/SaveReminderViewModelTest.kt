package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.CoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects

    private lateinit var saveReminderVM: SaveReminderViewModel
    private lateinit var source: FakeDataSource


    @get:Rule
    var coroutineRule = CoroutineRule()

    @get:Rule
    var executorRule = InstantTaskExecutorRule()

    @Before
    fun setUpViewModel() {
        source = FakeDataSource()
        saveReminderVM =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), source)
    }

    @After
    fun stoppingKoin() {
        stopKoin()
    }

    @Test
    fun getData_withoutTitle_returnNegative() {
        //with
        val location = "location"
        val description = "description"
        val reminderDataItem = ReminderDataItem(
            null, description, location,
            null, null
        )

        val entered = saveReminderVM.validateEnteredData(reminderDataItem)
        val snack = saveReminderVM.showSnackBarInt.getOrAwaitValue()

        MatcherAssert.assertThat(entered, CoreMatchers.`is`(false))
        MatcherAssert.assertThat(snack, CoreMatchers.`is`(R.string.err_enter_title))
    }

    @Test
    fun getData_confirmData_returnsPositive() {
        //with
        val title = "title"
        val description = "description"
        val location = "location"
        val dataItem = ReminderDataItem(
            title, description, location,
            null, null
        )

        val entered = saveReminderVM.validateEnteredData(dataItem)

        MatcherAssert.assertThat(entered, CoreMatchers.`is`(true))
    }

    @Test
    fun test_load() = coroutineRule.runBlockingTest {

        val reminderDataItem = ReminderDataItem(
            "title",
            "description",
            "location",
            0.0,
            0.0
        )

        coroutineRule.pauseDispatcher()
        saveReminderVM.validateAndSaveReminder(reminderDataItem)
        val showLoadingBefore = saveReminderVM.showLoading.getOrAwaitValue()
        MatcherAssert.assertThat(showLoadingBefore, CoreMatchers.`is`(true))

        coroutineRule.resumeDispatcher()
        val showLoadingThen = saveReminderVM.showLoading.getOrAwaitValue()
        MatcherAssert.assertThat(showLoadingThen, CoreMatchers.`is`(false))
    }

    @Test
    fun getData_withoutLocation_returnNegative() {
        //with
        val title = "title"
        val description = "description"
        val reminderDataItem = ReminderDataItem(
            title, description, null,
            null, null
        )

        val entered = saveReminderVM.validateEnteredData(reminderDataItem)
        val snack = saveReminderVM.showSnackBarInt.getOrAwaitValue()

        MatcherAssert.assertThat(snack, CoreMatchers.`is`(R.string.err_select_location))
        MatcherAssert.assertThat(entered, CoreMatchers.`is`(false))
    }

    @Test
    fun onClear() = coroutineRule.runBlockingTest {
        saveReminderVM.onClear()

        MatcherAssert.assertThat(
            saveReminderVM.reminderSelectedLocationStr.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )

        MatcherAssert.assertThat(
            saveReminderVM.reminderTitle.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
        MatcherAssert.assertThat(
            saveReminderVM.selectedPOI.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
        MatcherAssert.assertThat(
            saveReminderVM.longitude.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
        MatcherAssert.assertThat(
            saveReminderVM.latitude.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
        MatcherAssert.assertThat(
            saveReminderVM.reminderDescription.getOrAwaitValue(),
            CoreMatchers.`is`(CoreMatchers.nullValue())
        )
    }
}
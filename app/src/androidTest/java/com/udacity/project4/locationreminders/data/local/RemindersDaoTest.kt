package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

//    TODO: Add testing implementation to the RemindersDao.kt

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTask = InstantTaskExecutorRule()
    private lateinit var db: RemindersDatabase

    @Before
    fun initData() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeData() {
        db.close()
    }

    @Test
    fun save_andGetReminderById() {

        runBlockingTest {

            val reminderDTO = ReminderDTO(
                "México",
                "Palacio de las Bellas artes",
                "Ciudad de México",
                19.435422571447983,
                -99.14121073197735
            )

            val reminderDao = db.reminderDao()
            reminderDao.saveReminder(reminderDTO)

            //When
            val loaded = reminderDao.getReminderById(reminderDTO.id)

            //Then
            assertThat<ReminderDTO>(
                loaded as ReminderDTO,
                notNullValue()
            )
            assertThat(
                loaded.title,
                `is`(reminderDTO.title)
            )
            assertThat(
                loaded.description,
                `is`(reminderDTO.description)
            )
            assertThat(
                loaded.id,
                `is`(reminderDTO.id)
            )
        }
    }

    @Test
    fun getReminders() {

        runBlockingTest {
            //Given
            val reminders = db.reminderDao().getReminders()

            //When
            val loaded = db.reminderDao().getReminders()

            //Then
            assertThat(loaded, `is`(reminders))
        }
    }

    @Test
    fun noFound_shouldReturnError() = runBlockingTest {

        val reminderDTO = ReminderDTO(
            "México",
            "Palacio de las Bellas artes",
            "Ciudad de México",
            19.435422571447983,
            -99.14121073197735
        )
        //When
        val loaded =
            db.reminderDao()
                .getReminderById(reminderDTO.id)

        //Then
        assertThat(
            loaded,
            `is`(CoreMatchers.nullValue())
        )
    }

}
package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.*

/**
 * Concrete implementation of a data source as a db.
 *
 * The repository is implemented so that you can focus on only testing it.
 *
 * @param remindersDao the dao that does the Room db operations
 * @param ioDispatcher a coroutine dispatcher to offload the blocking IO tasks
 */
class RemindersLocalRepository(
    private val remindersDao: RemindersDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReminderDataSource {

    /**
     * Get the reminders list from the local db
     * @return Result the holds a Success with all the reminders or an Error object with the error message
     */
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return withContext(ioDispatcher) {
            EspressoIdlingResource.increment()
            try {
                Result.Success(remindersDao.getReminders())
            } catch (ex: Exception) {
                Result.Error(ex.localizedMessage)
            } finally {
                EspressoIdlingResource.decrement()
            }
        }
    }

    /**
     * Insert a reminder in the db.
     * @param reminder the reminder to be inserted
     */
    override suspend fun saveReminder(reminder: ReminderDTO) {
        EspressoIdlingResource.increment()
        withContext(ioDispatcher) {
            remindersDao.saveReminder(reminder)
            EspressoIdlingResource.decrement()
        }
    }

    /**
     * Get a reminder by its id
     * @param id to be used to get the reminder
     * @return Result the holds a Success object with the Reminder or an Error object with the error message
     */
    override suspend fun getReminder(id: String): Result<ReminderDTO> = withContext(ioDispatcher) {
        EspressoIdlingResource.increment()
        try {
            val reminder = remindersDao.getReminderById(id)
            if (reminder != null) {
                EspressoIdlingResource.decrement()
                return@withContext Result.Success(reminder)
            } else {
                EspressoIdlingResource.decrement()
                return@withContext Result.Error("Reminder not found!")
            }
        } catch (e: Exception) {
            EspressoIdlingResource.decrement()
            return@withContext Result.Error(e.localizedMessage)
        }
    }

    /**
     * Deletes all the reminders in the db
     */
    override suspend fun deleteAll() {
        EspressoIdlingResource.increment()
        withContext(ioDispatcher) {
            remindersDao.deleteAllReminders()
            EspressoIdlingResource.decrement()
        }
    }
}

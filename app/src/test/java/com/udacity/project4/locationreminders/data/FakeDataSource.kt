package com.udacity.project4.locationreminders.data

import android.util.Log
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source

    var reminderList: MutableList<ReminderDTO> = mutableListOf()
    private var error = false

    init {
        reminderList = mutableListOf()
        setError(false)
    }

    fun setError(error: Boolean) {
        this.error = error
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (error) {
            Result.Error("TEST-Error")
        } else {
            Result.Success(ArrayList(reminderList))
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList.add(reminder)
    }


    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (error) {
            Result.Error("TEST-Error")
        } else {

            val found = reminderList.filter { item ->
                item.id == id
            }

            if (found.isNotEmpty()) {
                Result.Success(found[0])
            } else {
                Result.Error("Reminder not found")
            }
        }
    }

    override suspend fun deleteAll() {
        reminderList.clear()
    }
}
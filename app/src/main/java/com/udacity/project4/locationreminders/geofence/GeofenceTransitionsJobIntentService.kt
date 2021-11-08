package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        //        TODO: call this to start the JobIntentService to handle the geofencing transition events
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        //TODO: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //TODO call @sendNotification
        val geofencingEventFromIntent = GeofencingEvent.fromIntent(intent)
        when (geofencingEventFromIntent.hasError()) {
            true -> {
                Log.i(
                    "TEST",
                    "GeofenceTransitionsJobIntentService.onHandleWork ${geofencingEventFromIntent.errorCode.toString()}"
                )
                return
            }
        }

        val trigger: MutableList<Geofence> = mutableListOf()
        when (Geofence.GEOFENCE_TRANSITION_DWELL == geofencingEventFromIntent.geofenceTransition ||
                Geofence.GEOFENCE_TRANSITION_ENTER == geofencingEventFromIntent.geofenceTransition
        ) {
            true -> {
                Log.i(
                    "TEST",
                    "GeofenceTransitionsJobIntentService.onHandleWork Geofence ${geofencingEventFromIntent.geofenceTransition.toString()}"
                )

                if (geofencingEventFromIntent.triggeringGeofences.isNotEmpty()) {
                    val item = geofencingEventFromIntent.triggeringGeofences[0]
                    trigger.add(item)
                    sendNotification(trigger)
                } else {
                    Log.e(
                        "TEST",
                        "GeofenceTransitionsJobIntentService.onHandleWork No Geofence Found"
                    )
                    return
                }
            }
        }

    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: List<Geofence>) {
        if (triggeringGeofences.isEmpty()) {
            Log.e(
                "TEST",
                "GeofenceTransitionsJobIntentService.sendNotification No Geofence"
            )
            return
        } else if (triggeringGeofences[0].requestId.isNullOrBlank()) {
            Log.e(
                "TEST",
                "GeofenceTransitionsJobIntentService.sendNotification No Geofence"
            )
            return
        }

        triggeringGeofences.forEach {
            val requestId = it.requestId

            //Get the local repository instance
            val remindersLocalRepository: RemindersLocalRepository by inject()
//        Interaction to the repository has to be through a coroutine scope
            CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                //get the reminder with the request id
                val result = remindersLocalRepository.getReminder(requestId)
                if (result is Result.Success<ReminderDTO>) {
                    val reminderDTO = result.data
                    //send a notification to the user with the reminder details
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                            reminderDTO.title,
                            reminderDTO.description,
                            reminderDTO.location,
                            reminderDTO.latitude,
                            reminderDTO.longitude,
                            reminderDTO.id
                        )
                    )
                }
            }
        }
    }
}
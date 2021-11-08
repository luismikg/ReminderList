package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private val BACKGROUND_LOCATION_REQUEST_CODE = 732
    private var DEVICE_LOCATION_ON = 733

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.lifecycleOwner = this

        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location

            checkBackgroundLocationPermission(true)
        }

        binding.saveReminder.setOnClickListener {

            val reminderDataItem = ReminderDataItem(
                title = _viewModel.reminderTitle.value,
                description = _viewModel.reminderDescription.value,
                location = _viewModel.reminderSelectedLocationStr.value,
                latitude = _viewModel.latitude.value,
                longitude = _viewModel.longitude.value
            )

            //Check the data and save
            if (_viewModel.validateEnteredData(reminderDataItem)) {
                checkDeviceLocation(reminderDataItem = reminderDataItem)
                //createGeofence(reminderDataItem)
            }
        }

        checkBackgroundLocationPermission()
    }

    @SuppressLint("UnspecifiedImmutableFlag", "MissingPermission")
    private fun createGeofence(reminderDataItem: ReminderDataItem) {

        if (reminderDataItem.latitude != null && reminderDataItem.longitude != null) {

            val geofence = Geofence.Builder()
                .setRequestId(reminderDataItem.id)
                .setCircularRegion(
                    reminderDataItem.latitude!!,
                    reminderDataItem.longitude!!,
                    GEOFENCE_RADIUS
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()

            getPendingIntent()?.let {
                geofencingClient.addGeofences(geofencingRequest, it).run {
                    addOnSuccessListener {
                        //SAve
                        _viewModel.validateAndSaveReminder(reminderDataItem)
                    }
                    addOnFailureListener {
                        Log.i(
                            "TEST",
                            "SaveReminderFragment.createGeofence ERROR: getPendingIntent"
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(
            requireActivity().applicationContext,
            GeofenceBroadcastReceiver::class.java
        ).apply {
            action = GEOFENCE_ACTION_EVENT
        }

        return PendingIntent.getBroadcast(
            requireActivity(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @TargetApi(30)
    private fun checkBackgroundLocationPermission(navigate: Boolean = false) {

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                BACKGROUND_LOCATION_REQUEST_CODE
            )

            if (navigate) {

                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
            }
        } else {

            if (navigate) {
                _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
            }
        }
    }

    private fun checkDeviceLocation(reminderDataItem: ReminderDataItem) {

        val builder = LocationSettingsRequest.Builder().addLocationRequest(
            LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_LOW_POWER
            }
        )

        val locationSettings = LocationServices.getSettingsClient(
            requireContext()
        ).checkLocationSettings(builder.build())

        locationSettings.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        requireActivity(),
                        DEVICE_LOCATION_ON
                    )
                } catch (error: IntentSender.SendIntentException) {
                    Log.e(
                        "TEST",
                        "SaveReminderFragment.checkDeviceLocation ERROR: ${error.message}"
                    )
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_denied_explanation),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        locationSettings.addOnCompleteListener {
            if (it.isSuccessful) {
                createGeofence(reminderDataItem)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    companion object {
        private const val GEOFENCE_RADIUS = 50f
        private const val GEOFENCE_ACTION_EVENT = "com.udacity.project4.action.geofence_event"

    }
}

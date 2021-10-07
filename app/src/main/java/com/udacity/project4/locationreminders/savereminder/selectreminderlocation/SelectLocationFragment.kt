package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : OnMapReadyCallback, GoogleMap.OnMarkerClickListener, BaseFragment() {

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 2053
    private lateinit var lastLocation: Location
    private lateinit var pointOfInterestSelected: PointOfInterest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        initMap()
        initGetCurrentLocation()
        onLocationSelected()

        return binding.root
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initGetCurrentLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private fun onLocationSelected() {
        binding.saveButton.setOnClickListener {
            try {
                _viewModel.apply {
                    selectedPOI.value = pointOfInterestSelected

                    initSelectedLocation()
                    navigationCommand.value = NavigationCommand.Back
                }
            } catch (e: Exception) {
                Log.i(
                    "TEST",
                    "SelectLocationFragment.onLocationSelected ${e.message}"
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(p0: GoogleMap?) {
        p0?.let {
            googleMap = p0
            configMap()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun configMap() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

            requestPermissions(permissions, LOCATION_REQUEST_CODE)
            return
        }
        moveToUserLocation()

        googleMap.setOnPoiClickListener { pointOfInterest ->
            setMarkerMap(pointOfInterest)
        }

        mapStyle()
    }

    private fun setMarkerMap(pointOfInterest: PointOfInterest) {

        googleMap.clear()

        val markerOptions = MarkerOptions()
            .position(pointOfInterest.latLng)
            .title(pointOfInterest.name)

        val poiMarker = googleMap.addMarker(markerOptions)
        pointOfInterestSelected = pointOfInterest
        poiMarker.showInfoWindow()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(
            "TEST",
            "SelectLocationFragment.onRequestPermissionsResult"
        )
        moveToUserLocation()
    }

    @SuppressLint("MissingPermission")
    private fun moveToUserLocation() {
        googleMap.isMyLocationEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                lastLocation = location

                val currentLatLng = LatLng(location.latitude, location.longitude)
                val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f)

                googleMap.animateCamera(cameraUpdateFactory)
            }
        }
    }

    private fun mapStyle() {
        try {
             val loadedMap = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )

            Log.i(
                "TEST",
                "SelectLocationFragment.mapStyle: $loadedMap"
            )

        } catch (e: Resources.NotFoundException) {
            Log.e(
                "TEST",
                "SelectLocationFragment.mapStyle ${e.message}"
            )
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean = false
}

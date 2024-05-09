package com.example.track.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel

class LocationViewModel(application: Application) : ViewModel() {

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> get() = _location

    private val _locationPermissionGranted = MutableLiveData<Boolean>()
    val locationPermissionGranted: LiveData<Boolean> get() = _locationPermissionGranted

    private var isLocationUpdatesRequested = false

    private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        _locationPermissionGranted.value = checkLocationPermission(application)
    }

    // Function to request location updates
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(application: Application) {
        if (!isLocationUpdatesRequested) {
            if (isLocationPermissionGranted(application)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    locationListener
                )
                isLocationUpdatesRequested = true
            }
        }
    }

    // Function to check if location permission is granted
    private fun isLocationPermissionGranted(application: Application): Boolean {
        return ActivityCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationPermission(application: Application): Boolean {
        return ContextCompat.checkSelfPermission(
            application, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Location listener to handle location updates
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _location.value = location
        }

        override fun onProviderDisabled(provider: String) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
    }
}
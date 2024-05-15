package com.example.track.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.track.repository.UserRepository
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

class BackgroundServices : Service() {
    lateinit var mContext: Context
    lateinit var userRepository : UserRepository



    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
            userRepository = UserRepository(mContext)
        if (isInternetAvailable()) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            ) {
                // Location services are disabled, prompt the user to enable them
                showLocationSettingsDialog()
            }
            myFunction()
        }
        checkInternetBackground()
    }

    private fun checkInternetBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                if (isInternetAvailable()) {
                    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                        !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    ) {
                        // Location services are disabled, prompt the user to enable them
                        showLocationSettingsDialog()
                    }
                    myFunction()
                }
                delay(900000)
            }
        }
    }

    private fun showLocationSettingsDialog() {
        Toast.makeText(this, "Location services are disabled.", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    private fun myFunction() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)

        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult?.lastLocation?.let { location ->
                // Handle the received location here
                val latitude = location.latitude
                val longitude = location.longitude
                Log.e("LocationUpdates", "Latitude: $latitude, Longitude: $longitude")
                val address = getAddress(latitude, longitude)

                CoroutineScope(Dispatchers.IO).launch {
                    userRepository.insertLocationUpdate(latitude,longitude,address)
                }

            }
        }

    }

    fun getAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(mContext, Locale.getDefault())
        var addressText = ""
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses!=null && addresses.isNotEmpty()) {
                val address = addresses[0]
                // Format the address as needed, e.g., combining address lines
                addressText = "${address.getAddressLine(0)}, ${address.locality}, ${address.countryName}"
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addressText
    }


    val locationRequest = LocationRequest.create().apply {
        interval = 10000 // Update interval in milliseconds
        fastestInterval = 5000 // Fastest update interval
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Accuracy requirement
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "MyBackgroundService"
    }
}

package com.example.track.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.track.MyApplication
import com.example.track.adapter.LocationListAdapter
import com.example.track.databinding.ActivityHomeBinding
import com.example.track.model.LocationUpdate
import com.example.track.model.LocationViewModel
import com.example.track.model.UserViewModel
import com.example.track.model.UserViewModelFactory
import com.example.track.repository.UserRepository
import com.example.track.service.BackgroundServices
import com.example.track.service.LocationViewModelFactory
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity(),LocationListAdapter.OnItemClickListener {

    lateinit var binding : ActivityHomeBinding

    lateinit var mContext : Context

    val locationViewModel: LocationViewModel by viewModels {
        LocationViewModelFactory(this.application)
    }

    var isLocationObserverRegistered = false
    var isLocationUpdatesRequested = false

    val PERMISSIONS =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    lateinit var viewModel : UserViewModel
    lateinit var userRepository  : UserRepository

    lateinit var listener : LocationListAdapter.OnItemClickListener
    private var myServiceIntent: Intent? = null

    private var thread1 : Job? = null
    private var thread2 : Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MyApplication.activity = this
        mContext = this
        listener = this
        userRepository = UserRepository(mContext)

        val factory = UserViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

        binding.logout.setOnClickListener {

           // Clear data and sign out
            CoroutineScope(Dispatchers.Main).launch {
                if(userRepository.getLoggedUser().get(0).password.equals("")){
                    val auth = FirebaseAuth.getInstance()
                    auth.signOut()
                }
                userRepository.deleteUsers()
                userRepository.deleteLoggedUsers()
                userRepository.deleteLocationHistory()
                thread1?.cancel()
                thread2?.cancel()
                stopBackgroundService()
            }
            val intent = Intent(this@HomeActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }


        askPermissions()
        thread1 = CoroutineScope(Dispatchers.Main).launch {
            var locations = userRepository.getAllLoctionHistory()
            binding.recycler.layoutManager = LinearLayoutManager(mContext)
            binding.recycler.adapter = LocationListAdapter(mContext, locations, listener)
        }

        fetchDetails()
        if(!isLocationEnabled(this)){
            showLocationSettingsDialog()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        thread1?.cancel()
        thread2?.cancel()
        stopBackgroundService()
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun stopBackgroundService() {
        myServiceIntent?.let {
            stopService(it)
            myServiceIntent = null
        }
    }

    private fun showLocationSettingsDialog() {
        Toast.makeText(this, "Location services are disabled.", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    fun fetchDetails(){
        thread2 = CoroutineScope(Dispatchers.Main).launch {
                var locations = userRepository.getAllLoctionHistory()
                binding.recycler.layoutManager = LinearLayoutManager(mContext)
                var locationAdapter = LocationListAdapter(mContext, locations, listener)
                binding.recycler.adapter = locationAdapter
                while (true) {
                if (isInternetAvailable()) {
                    var locations = userRepository.getAllLoctionHistory()
                    binding.recycler.layoutManager = LinearLayoutManager(mContext)
                    var locationAdapter = LocationListAdapter(mContext, locations, listener)
                    binding.recycler.adapter = locationAdapter

                } else {
                    Toast.makeText(mContext,"Please Turn on your Internet",Toast.LENGTH_SHORT).show()
                }
                    delay(10000)
            }
        }
    }

    fun askPermissions(){
        if(!checkPermission()){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
        }else{

            myServiceIntent  = Intent(this, BackgroundServices::class.java)
            startService(myServiceIntent )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                // Check if permissions were granted
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(mContext, "granted", Toast.LENGTH_SHORT).show()
                    askPermissions()

                } else {
                    Toast.makeText(mContext, "rejected", Toast.LENGTH_SHORT).show()
                    askPermissions()
                }
            }
        }
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }
    fun checkPermission(): Boolean {
        val location = ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val courseLocation = ContextCompat.checkSelfPermission( mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return location && courseLocation
    }

    fun getLocation(homeActivity: HomeActivity) {
        locationViewModel.location.observe(homeActivity, Observer { location ->
            // Handle location updates
            val latitude = location?.latitude
            val longitude = location?.longitude
            Log.e("Location","Latitude::::"+latitude+", Longitude::::"+longitude)

            if (!isLocationObserverRegistered) {
                locationViewModel.location.removeObservers(homeActivity)

                isLocationObserverRegistered = true
            }
        })
        isLocationUpdatesRequested = false
        requestLocationEnable(this,locationViewModel)

    }
    val locationRequest = LocationRequest().apply {
        interval = 10000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    fun requestLocationEnable(activity: FragmentActivity, locationViewModel: LocationViewModel) {

        if (!isLocationUpdatesRequested) {
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val task = LocationServices.getSettingsClient(mContext).checkLocationSettings(builder.build())

            task.addOnSuccessListener { response ->
                val status = response.locationSettingsStates
                if (status!!.isLocationPresent) {
                    locationViewModel.requestLocationUpdates(activity.application)
                    isLocationUpdatesRequested = true
                }
            }.addOnFailureListener { e ->

                val statusCode = (e as ResolvableApiException).statusCode
                if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    try {
                        Toast.makeText(mContext, "Enable the Location", Toast.LENGTH_SHORT).show()
                        e.startResolutionForResult(activity, 103)
                    } catch (exception: IntentSender.SendIntentException) {
                    }
                }
            }
        }
    }

    override fun onGoingResponseRootLayoutClicked(model: LocationUpdate) {
        val mapIntent = Intent(this,MapActivity::class.java)
        mapIntent.putExtra("latitude",model.latitude)
        mapIntent.putExtra("longitude",model.longitude)
        mapIntent.putExtra("id",model.id)
        startActivity(mapIntent)
    }

    companion object{

    }
}
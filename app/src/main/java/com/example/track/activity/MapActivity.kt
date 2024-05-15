package com.example.track.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.track.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.track.databinding.ActivityMapBinding
import com.example.track.model.LocationUpdate
import com.example.track.repository.UserRepository
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapBinding
    private val locationPermissionCode = 1

    lateinit var mContext : Context

    var latitude by Delegates.notNull<Double>()
    var longitude by Delegates.notNull<Double>()
    var id by Delegates.notNull<Long>()
    lateinit var origin : LatLng
    lateinit var destination : LatLng
    lateinit var googleMap: GoogleMap
    private lateinit var geoApiContext: GeoApiContext

    lateinit var userRepository  : UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this
        userRepository = UserRepository(mContext)


        latitude = intent.getDoubleExtra("latitude",0.0)
        longitude = intent.getDoubleExtra("longitude",0.0)
        id = intent.getLongExtra("id",0)

        origin = LatLng(latitude,longitude)
//        destination = LatLng(11.361179,77.694011)
        destination = LatLng(9.968306,78.126657)
        geoApiContext = GeoApiContext.Builder()
            .apiKey(getString(R.string.myMapApiKey))
            .build()
        checkLocationPermission()

        binding.playback.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val dbHistory = userRepository.getAllLocationHistory()
                var locationHistory : MutableList<LocationUpdate> = mutableListOf()
                for(i in dbHistory.size-1 downTo 1){
                    if(dbHistory.get(i).id <=  id){
                        locationHistory.add(dbHistory.get(i))
                    }
                }
                setDirections(dbHistory)
              /*  var count =0
                for(location in locationHistory){
                    if(count==0){
                        count+=1
                        continue
                    }
                    count+=1



                    if(location.id == locationHistory.get(locationHistory.size-1).id){
                        return@launch
                    }
                }*/
            }

        }
    }

    private fun setDirections(
        dbHistory: List<LocationUpdate>
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            var currentId = id

            var directionList : MutableList<LatLng> = mutableListOf()
            val looping = async { for(i in currentId downTo dbHistory.get(0).id){
                    val point = LatLng(dbHistory.get(i.toInt()).latitude,dbHistory.get(i.toInt()).longitude)
                    directionList.add(point)
                    googleMap.addMarker(MarkerOptions().position(point).title("stop"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 14f))
                    mMap.animateCamera(CameraUpdateFactory.zoomIn(),3000 ,null)
            } }
            looping.await()
            if(directionList.size>2){
                fetchDirections(directionList)
                CoroutineScope(Dispatchers.Main).launch {
                    for (i in 0 until directionList.size) {
                        val destination = directionList[i]
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 14f))
                        mMap.animateCamera(CameraUpdateFactory.zoomOut(), 3000, null)
                        delay(2000)
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(300f), 2000, null)
                    }
                }
            }
        }



    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            loadMap()
        }
    }

    private fun loadMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadMap()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        googleMap = map


//        move camera to show both markers
      /*  val bounds = LatLngBounds.Builder()
            .include(origin)
            .include(destination)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,100))*/
//        fetchDirections(origin,destination)

        mMap.addMarker(MarkerOptions().position(origin).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 14f))
        Handler().postDelayed({
            mMap.animateCamera(CameraUpdateFactory.zoomIn(),3000 ,null)
        },3000)

    }

    // Example method to fetch directions with multiple waypoints
    private fun fetchDirections(directionList: MutableList<LatLng>) {
        // Check if there are enough locations to create a route
        if (directionList.size < 2) {
            Log.e("MapsActivity", "Insufficient locations for directions")
            return
        }

        // Initialize Directions API requests for each leg of the route
        for (i in 0 until directionList.size - 1) {
            val origin = directionList[i]
            val destination = directionList[i + 1]

            val directionsApi = DirectionsApi.newRequest(geoApiContext)
            directionsApi.origin(com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.DRIVING) // You can change the travel mode as needed

            // Execute the Directions API request for each leg
            directionsApi.setCallback(object : PendingResult.Callback<DirectionsResult> {
                override fun onResult(result: DirectionsResult?) {
                    runOnUiThread {
                        result?.routes?.let { routes ->
                            if (routes.isNotEmpty()) {
                                // Process each route (if multiple routes are returned)
                                for (route in routes) {
                                    val overviewPolyline = route.overviewPolyline
                                    val points = overviewPolyline.decodePath()

                                    val polylineOptions = PolylineOptions()
                                    for (point in points) {
                                        polylineOptions.add(LatLng(point.lat, point.lng))
                                    }
                                    polylineOptions.width(10f)
                                    polylineOptions.color(Color.BLUE)
                                    googleMap.addPolyline(polylineOptions)
                                }
                            } else {
                                Log.e("MapsActivity", "No routes found")
                            }
                        }
                    }
                }

                override fun onFailure(e: Throwable?) {
                    runOnUiThread {
                        Log.e("MapsActivity", "Error fetching directions: ${e?.message}")
                    }                }
            })
        }
    }




}

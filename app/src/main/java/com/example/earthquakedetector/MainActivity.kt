package com.example.earthquakedetector

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.earthquakedetector.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import android.app.NotificationChannel
import android.app.NotificationManager
import java.util.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback{
    val minmagnitudeinput: Double?=null
    val maxrangeinput: Int?=null
    val intervalinput: Int= 0
    val NOTIF_ID = 0
    private val pERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var mMap: GoogleMap
    // Current location set to Samos, no use for the project except for debugging purposes
    var currentLocation: LatLng = LatLng(37.79633740905522, 26.707267207061236)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        val apiKey = value.toString()

        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        // Initializing Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initializing fused location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Adding functionality to buttons
        binding.currentLoc.setOnClickListener(){ getLastLocation() }
        binding.settingsButton.setOnClickListener(){
            val intent = Intent(this, com.example.earthquakedetector.Preferences::class.java);
            startActivity(intent)
        }
        //Used to obtain the maximum range rad and minimum magnitude from Preferences activity
        if (maxrangeinput != null) {
            intent.getIntExtra("maxRange", maxrangeinput)
        }
        if (minmagnitudeinput != null) {
            intent.getDoubleExtra("minMagnitude", minmagnitudeinput)
        };
        if (intervalinput != null) {
            intent.getIntExtra("minInterval", intervalinput)
        };
        val readfile = ReadFile("https://bbnet2.gein.noa.gr/current_catalogue/current_catalogue_year2.php")
        val actualmagnitude = readfile.getMagnitude()
        val actualmaxrange = readfile.getRange()
        val myIntent = Intent(this, MainActivity::class.java)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getService(this, 0, myIntent, 0)
        var calendar = Calendar.getInstance();
        val builder = NotificationCompat.Builder(this, "notification").setContentIntent(pendingIntent).setSmallIcon(R.drawable.warning48px).setContentTitle("Προειδοποίηση σεισμού").setContentText("Εντοπίστηκεs σεισμός μεγέθους  ${actualmagnitude} σε απόσταση ακτίνας  ${actualmaxrange} km ώρα ${readfile.getHour()} : ${readfile.getMinutes()}").setPriority(NotificationCompat.PRIORITY_DEFAULT)
        calendar.set(Calendar.MINUTE, 0)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, (1000*intervalinput).toLong(), pendingIntent);
        val notifManger = NotificationManagerCompat.from(this)
        if (minmagnitudeinput!! <= actualmagnitude!! &&  maxrangeinput!! < actualmaxrange!!){
            notifManger.notify(NOTIF_ID, builder)
        }
    }

    // Services such as getLastLocation()
    // will only run once map is ready
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        getLastLocation()
    }

    // Get current location
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        //If permissions are met
        if (checkPermissions()) {
            //If location is enabled
            if (isLocationEnabled()) {
                //
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        //Adds a marker to the location of the device
                        currentLocation = LatLng(location.latitude, location.longitude)
                        mMap.clear()
                        mMap.addMarker(MarkerOptions().position(currentLocation).draggable(true))//Adding a marker to our position
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16F))//Speed of camera
                        mMap.setOnMarkerDragListener(object : OnMarkerDragListener {
                            override fun onMarkerDragStart(marker: Marker) {
                                // TODO Auto-generated method stub
                            }

                            override fun onMarkerDragEnd(marker: Marker) {
                                // TODO Auto-generated method stub
                                location.latitude = marker.position.latitude
                                location.longitude = marker.position.longitude
                            }

                            override fun onMarkerDrag(marker: Marker) {
                                // TODO Auto-generated method stub
                            }
                        })
                    }
                }
            } else {
                //
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    // Get current location, if shifted
    // from previous location
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    // If current location could not be located, use last location
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            if (mLastLocation != null) {
                currentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            }
        }
    }

    // function to check if GPS is on
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // Check if location permissions are
    // granted to the application
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // Request permissions if not granted before
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            pERMISSION_ID
        )
    }

    // What must happen when permission is granted
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == pERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    //Function to update the latest earthquake using user's Preferences for handling notifications.
    //If the user's magnitude is smaller than the actual and the surrounding range is smaller than actual, it sends a notification to the user
    //that an earthquake just happened
}
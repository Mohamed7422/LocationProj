package com.example.locationproj

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.locationproj.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlin.toString as toString

const val MY_LOCATION_PERMISSION_ID =5005
private const val TAG ="Activity Main Tag"
class MainActivity : AppCompatActivity() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    lateinit var geocoder: Geocoder

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geocoder = Geocoder(this)

        locationCallback = object : LocationCallback() { //returned location
            override fun onLocationResult(location: LocationResult) {
                val lastLocation = location.lastLocation

                val intent = Intent(this@MainActivity, MapsActivity::class.java)

                val addresses =
                    lastLocation?.let {
                        geocoder.getFromLocation(it.latitude,it.longitude,1)
                    }
                val address = addresses?.get(0)
                Log.i(TAG,"address Location ${address.toString()}")
                val function: (v: View) -> Unit = {

                    intent.putExtra(getString(R.string.LastLocationIntentKey), lastLocation)
                    startActivity(intent)
                }
                binding.location.setOnClickListener(function)

            }
           }
          }

    override fun onStart() {
        super.onStart()
        getLocation()
    }



    //requestPermission


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_LOCATION_PERMISSION_ID){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED || grantResults[1]==PackageManager.PERMISSION_GRANTED ){
                println("=================${grantResults.size}==================")
              getLocation()

            }
        }
    }




    private fun getLocation() {
        if (isGpsAndNetworkPermissionsGranted()){
            if (isLocationEnabled()){
                requestNewLocationData() //get new Location fresh
            }else {
                Toast.makeText(this,"Kindly open location",Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

                startActivity(intent)
            }
        }else{
            requestPermission()
        }
    }

    private fun isGpsAndNetworkPermissionsGranted() :Boolean{
        val result  = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED  || ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return result
    }
    private fun isLocationEnabled():Boolean{
        val locationManager:LocationManager =
            getSystemService(Context.LOCATION_SERVICE)as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)

    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval =500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        fusedClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())

    }
    private fun requestPermission(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),
            MY_LOCATION_PERMISSION_ID
        )
         }
    }

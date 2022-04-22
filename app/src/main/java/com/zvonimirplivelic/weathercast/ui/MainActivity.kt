package com.zvonimirplivelic.weathercast.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.zvonimirplivelic.weathercast.R
import com.zvonimirplivelic.weathercast.util.Constants.LOCATION_REQUEST_CODE


class MainActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fetchLocation()

    }

    private fun fetchLocation() {
        val task = fusedLocationProviderClient.lastLocation.also {
            if (checkLocationPermission()) return

            it.addOnSuccessListener { location ->
                if (location != null) {
                    Toast.makeText(
                        applicationContext,
                        "Lat: ${location.latitude} Lon: ${location.longitude}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return true
        }
        return false
    }
}
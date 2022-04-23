package com.zvonimirplivelic.weathercast.ui

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.zvonimirplivelic.weathercast.R
import com.zvonimirplivelic.weathercast.WeatherCastViewModel
import com.zvonimirplivelic.weathercast.util.Constants
import com.zvonimirplivelic.weathercast.util.Resource
import timber.log.Timber

class CurrentWeatherFragment : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var viewModel: WeatherCastViewModel
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_current_weather, container, false)

        viewModel = ViewModelProvider(this)[WeatherCastViewModel::class.java]
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        progressBar = view.findViewById(R.id.progress_bar)

        fetchLocation()

        viewModel.weatherData.observe(viewLifecycleOwner) { response ->
            when (response) {

                is Resource.Success -> {
                    progressBar.isVisible = false
                    response.data?.let { weatherResponse ->
                        Timber.d("ResponseMain: ${weatherResponse}")
                    }
                }

                is Resource.Error -> {
                    progressBar.isVisible = false
                    Timber.d("ResponseResErr: $response Resmes:${response.message}")
                    response.message?.let { message ->
                        Toast.makeText(
                            requireContext(),
                            "An error occured: $message",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        Timber.d("ResponseMessage: $message")
                    }
                }

                is Resource.Loading -> {
                    progressBar.isVisible = true
                }
            }
        }

        return view
    }

    private fun fetchLocation() {
        val appInfo: ApplicationInfo = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
        val apiKey = appInfo.metaData["apiKey"].toString()

        fusedLocationProviderClient.lastLocation.also { task ->
            if (checkLocationPermission()) return

            task.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude.toString()
                    val lon = location.longitude.toString()

                    viewModel.getWeatherData(lat, lon, apiKey)
                }
            }
        }
    }
    
    private fun checkLocationPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.LOCATION_REQUEST_CODE
            )
            return true
        }
        return false
    }
}
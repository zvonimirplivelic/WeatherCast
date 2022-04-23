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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.zvonimirplivelic.weathercast.R
import com.zvonimirplivelic.weathercast.WeatherCastViewModel
import com.zvonimirplivelic.weathercast.model.WeatherResponse
import com.zvonimirplivelic.weathercast.util.Constants
import com.zvonimirplivelic.weathercast.util.Resource
import timber.log.Timber

class CurrentWeatherFragment : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var viewModel: WeatherCastViewModel
    private lateinit var weatherData: WeatherResponse

    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var tvLocationName: TextView
    private lateinit var tvUpdatedTime: TextView
    private lateinit var tvCurrentTemperature: TextView
    private lateinit var tvMinMaxTemperature: TextView
    private lateinit var tvFeelsLikeTemperature: TextView
    private lateinit var tvWeatherDescription: TextView
    private lateinit var tvAirPressure: TextView
    private lateinit var tvAirHumidity: TextView
    private lateinit var tvWindSpeed: TextView
    private lateinit var tvWindDirection: TextView
    private lateinit var tvVisibility: TextView
    private lateinit var tvSunriseTime: TextView
    private lateinit var tvSunsetTime: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_current_weather, container, false)

        viewModel = ViewModelProvider(this)[WeatherCastViewModel::class.java]
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        progressBar = view.findViewById(R.id.progress_bar)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        tvLocationName = view.findViewById(R.id.tv_location_name)
        tvCurrentTemperature = view.findViewById(R.id.tv_current_temperature)
        tvMinMaxTemperature = view.findViewById(R.id.tv_min_max_temperature)
        tvFeelsLikeTemperature = view.findViewById(R.id.tv_feels_like_temperature)
        tvWeatherDescription = view.findViewById(R.id.tv_weather_description)
        tvAirPressure = view.findViewById(R.id.tv_air_pressure)
        tvAirHumidity = view.findViewById(R.id.tv_air_humidity)
        tvWindSpeed = view.findViewById(R.id.tv_wind_speed)
        tvWindDirection = view.findViewById(R.id.tv_wind_direction)
        tvVisibility = view.findViewById(R.id.tv_visibility)
        tvUpdatedTime = view.findViewById(R.id.tv_updated_time)
        tvSunriseTime = view.findViewById(R.id.tv_sunrise_time)
        tvSunsetTime = view.findViewById(R.id.tv_sunset_time)

        fetchLocation()

        viewModel.weatherData.observe(viewLifecycleOwner) { response ->
            when (response) {

                is Resource.Success -> {
                    progressBar.isVisible = false
                    response.data?.let { weatherResponse ->
                        Timber.d("ResponseMain: $weatherResponse")
                        weatherData = weatherResponse

                        weatherData.let { weatherData ->
                            tvLocationName.text = weatherData.name
                            tvCurrentTemperature.text = weatherData.main.temp.toString()
                            tvMinMaxTemperature.text = "${weatherData.main.tempMin} / ${weatherData.main.tempMax}"
                            tvFeelsLikeTemperature.text = weatherData.main.feelsLike.toString()
                            tvWeatherDescription.text = weatherData.weather[0].description
                            tvAirPressure.text = weatherData.main.pressure.toString()
                            tvAirHumidity.text = weatherData.main.humidity.toString()
                            tvWindSpeed.text = weatherData.wind.speed.toString()
                            tvWindDirection.text = weatherData.wind.deg.toString()
                            tvVisibility.text = weatherData.visibility.toString()
                            tvUpdatedTime.text = weatherData.dt.toString()
                            tvSunsetTime.text = weatherData.sys.sunrise.toString()
                            tvSunriseTime.text = weatherData.sys.sunset.toString()
                        }
                    }
                }

                is Resource.Error -> {
                    progressBar.isVisible = false
                    response.message?.let { message ->
                        Toast.makeText(
                            requireContext(),
                            "Unable to fetch weather data: $message",
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
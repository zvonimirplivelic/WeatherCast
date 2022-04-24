package com.zvonimirplivelic.weathercast.ui

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt

class CurrentWeatherFragment : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var viewModel: WeatherCastViewModel
    private lateinit var weatherData: WeatherResponse

    private lateinit var layout: ConstraintLayout
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
    private lateinit var ivWindDirection: ImageView
    private lateinit var tvWindDirection: TextView
    private lateinit var tvClouds: TextView
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

        layout = view.findViewById(R.id.current_weather_layout)
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
        ivWindDirection = view.findViewById(R.id.iv_wind_direction)
        tvWindDirection = view.findViewById(R.id.tv_wind_direction)
        tvClouds = view.findViewById(R.id.tv_clouds)
        tvVisibility = view.findViewById(R.id.tv_visibility)
        tvUpdatedTime = view.findViewById(R.id.tv_updated_time)
        tvSunriseTime = view.findViewById(R.id.tv_sunrise_time)
        tvSunsetTime = view.findViewById(R.id.tv_sunset_time)

        fetchLocation()

        viewModel.weatherData.observe(viewLifecycleOwner) { response ->

            when (response) {

                is Resource.Success -> {
                    layout.isVisible = true
                    progressBar.isVisible = false
                    response.data?.let { weatherResponse ->
                        Timber.d("ResponseMain: $weatherResponse")
                        weatherData = weatherResponse

                        weatherData.let { weatherData ->

                            tvLocationName.text = weatherData.name
                            tvCurrentTemperature.text = resources.getString(
                                R.string.temperature_string,
                                convertTemperature(weatherData.main.temp)
                            )
                            tvMinMaxTemperature.text =
                                resources.getString(
                                    R.string.min_max_temperature_string,
                                    convertTemperature(weatherData.main.tempMin),
                                    convertTemperature(weatherData.main.tempMax)
                                )
                            tvFeelsLikeTemperature.text =
                                resources.getString(
                                    R.string.temperature_string,
                                    convertTemperature(weatherData.main.feelsLike)
                                )
                            tvWeatherDescription.text =
                                weatherData.weather[0].description.replaceFirstChar { it.uppercase() }
                            tvAirPressure.text = resources.getString(
                                R.string.air_pressure_string,
                                weatherData.main.pressure
                            )
                            tvAirHumidity.text = resources.getString(
                                R.string.percentage_string,
                                weatherData.main.humidity
                            )
                            tvWindSpeed.text =
                                resources.getString(
                                    R.string.kph_string,
                                    convertToKPH(weatherData.wind.speed)
                                )
                            ivWindDirection.rotation = weatherData.wind.deg.toFloat()
                            tvWindDirection.text = when ( weatherData.wind.deg) {
                                in 0..22 ->"N"
                                in 23..67 ->"NE"
                                in 68..112 ->"E"
                                in 113..157 ->"SE"
                                in 158..202 ->"S"
                                in 203..247 ->"SW"
                                in 248..292 ->"W"
                                in 293..337 ->"NW"
                                in 338..360 -> "N"
                                else -> "No direction"
                            }

                            tvClouds.text = resources.getString(
                                R.string.percentage_string,
                                weatherData.clouds.all
                            )
                            tvVisibility.text =
                                resources.getString(R.string.meters_string, weatherData.visibility)
                            tvUpdatedTime.text = convertTime(weatherData.dt)
                            tvSunriseTime.text = convertTime(weatherData.sys.sunrise)
                            tvSunsetTime.text = convertTime(weatherData.sys.sunset)
                        }
                    }
                }

                is Resource.Error -> {
                    layout.isVisible = false
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
                    layout.isVisible = false
                    swipeRefreshLayout.isRefreshing = false
                    progressBar.isVisible = true
                }
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            fetchLocation()
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

    private fun convertTemperature(value: Double): String {
        val convertedTemperature = value - 273.15
        val temperatureString = convertedTemperature.toString()
        return temperatureString.take(2)
    }

    private fun convertTime(time: Int, format: String = "HH:mm:ss"): String? {
        val instant =
            Instant.ofEpochSecond(time.toLong()).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern(format, Locale.ROOT)
        return instant.format(formatter)
    }

    private fun convertToKPH(value: Double): Int {
        return (value * 3.6).roundToInt()
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
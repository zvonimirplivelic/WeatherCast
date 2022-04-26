package com.zvonimirplivelic.weathercast.ui

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    private lateinit var parentLayout: FrameLayout
    private lateinit var childLayout: ConstraintLayout
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

    private lateinit var tvAirQualityIndex: TextView
    private lateinit var tvCOMeasurement: TextView
    private lateinit var tvNOMeasurement: TextView
    private lateinit var tvNO2Measurement: TextView
    private lateinit var tvO3Measurement: TextView
    private lateinit var tvSO2Measurement: TextView
    private lateinit var tvPM25Measurement: TextView
    private lateinit var tvPM10Measurement: TextView
    private lateinit var tvNH3Measurement: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_current_weather, container, false)

        viewModel = ViewModelProvider(this)[WeatherCastViewModel::class.java]
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        parentLayout = view.findViewById(R.id.current_weather_scroll_view)
        childLayout = view.findViewById(R.id.current_weather_layout)
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

        tvAirQualityIndex = view.findViewById(R.id.tv_air_quality_index)
        tvCOMeasurement = view.findViewById(R.id.tv_co_measurement)
        tvNOMeasurement = view.findViewById(R.id.tv_no_measurement)
        tvNO2Measurement = view.findViewById(R.id.tv_no2_measurement)
        tvO3Measurement = view.findViewById(R.id.tv_o3_measurement)
        tvSO2Measurement = view.findViewById(R.id.tv_so2_measurement)
        tvPM25Measurement = view.findViewById(R.id.tv_pm25_measurement)
        tvPM10Measurement = view.findViewById(R.id.tv_pm10_measurement)
        tvNH3Measurement = view.findViewById(R.id.tv_nh3_measurement)

        fetchLocation()

        viewModel.weatherData.observe(viewLifecycleOwner) { response ->

            when (response) {

                is Resource.Success -> {
                    parentLayout.isVisible = true
                    childLayout.isVisible = true
                    progressBar.isVisible = false
                    response.data?.let { weatherResponse ->
                        Timber.d("ResponseMain: $weatherResponse")
                        weatherData = weatherResponse

                        weatherData.let { weatherData ->

                            val weatherGraphicsCode = when (weatherData.weather[0].id) {
                                in 200..232 -> R.drawable.pic_thunderstorm
                                in 300..321 -> R.drawable.pic_drizzle
                                in 500..504 -> R.drawable.pic_rain
                                in 600..622 -> R.drawable.pic_snow
                                in 700..781 -> R.drawable.pic_mist
                                511 -> R.drawable.pic_snow
                                800 -> R.drawable.pic_clear
                                801 -> R.drawable.pic_clouds_light
                                802 -> R.drawable.pic_clouds_medium
                                803, 804 -> R.drawable.pic_clouds_heavy
                                else -> {
                                    R.drawable.bg_day
                                }
                            }

                            parentLayout.background =
                                ContextCompat.getDrawable(requireActivity(), weatherGraphicsCode)

                            childLayout.background =
                                if (weatherData.dt > weatherData.sys.sunrise &&
                                    weatherData.dt < weatherData.sys.sunset
                                ) {
                                    ContextCompat.getDrawable(
                                        requireActivity(),
                                        R.drawable.bg_day
                                    );
                                } else {
                                    ContextCompat.getDrawable(
                                        requireActivity(),
                                        R.drawable.bg_night
                                    );
                                }

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
                                    R.string.feels_like_temperature_string,
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
                            tvWindDirection.text = when (weatherData.wind.deg) {
                                in 0..22 -> "N"
                                in 23..67 -> "NE"
                                in 68..112 -> "E"
                                in 113..157 -> "SE"
                                in 158..202 -> "S"
                                in 203..247 -> "SW"
                                in 248..292 -> "W"
                                in 293..337 -> "NW"
                                in 338..360 -> "N"
                                else -> "No direction"
                            }

                            tvClouds.text = resources.getString(
                                R.string.percentage_string,
                                weatherData.clouds.all
                            )
                            tvVisibility.text =
                                resources.getString(R.string.meters_string, weatherData.visibility)
                            tvUpdatedTime.text = resources.getString(
                                R.string.updated_time_string,
                                convertTime(weatherData.dt)
                            )
                            tvSunriseTime.text = convertTime(weatherData.sys.sunrise)
                            tvSunsetTime.text = convertTime(weatherData.sys.sunset)
                        }
                    }
                }

                is Resource.Error -> {
                    parentLayout.isVisible = false
                    childLayout.isVisible = false
                    progressBar.isVisible = false
                    response.message?.let { message ->
                        Toast.makeText(
                            requireContext(),
                            "Unable to fetch weather data: $message",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }

                is Resource.Loading -> {
                    parentLayout.isVisible = false
                    childLayout.isVisible = false
                    swipeRefreshLayout.isRefreshing = false
                    progressBar.isVisible = true
                }
            }
        }

        viewModel.airPollutionWeatherData.observe(viewLifecycleOwner) { response ->
            when (response) {

                is Resource.Success -> {

                    parentLayout.isVisible = true
                    childLayout.isVisible = true
                    progressBar.isVisible = false

                    response.data?.let { airPollutionData ->

                        val pollutionData = airPollutionData.list[0]
                        tvAirQualityIndex.text = resources.getString(
                            R.string.air_quality_index_string,
                            pollutionData.main.aqi
                        )
                        tvCOMeasurement.text = resources.getString(
                            R.string.co_measurement_string,
                            pollutionData.components.co.toString().take(6)
                        )
                        tvNOMeasurement.text = resources.getString(
                            R.string.no_measurement_string,
                            pollutionData.components.no.toString().take(6)
                        )
                        tvNO2Measurement.text = resources.getString(
                            R.string.no2_measurement_string,
                            pollutionData.components.no2.toString().take(6)
                        )
                        tvO3Measurement.text = resources.getString(
                            R.string.o3_measurement_string,
                            pollutionData.components.o3.toString().take(6)
                        )
                        tvSO2Measurement.text = resources.getString(
                            R.string.so2_measurement_string,
                            pollutionData.components.so2.toString().take(6)
                        )
                        tvPM25Measurement.text = resources.getString(
                            R.string.pm2_5_measurement_string,
                            pollutionData.components.pm25.toString().take(6)
                        )
                        tvPM10Measurement.text = resources.getString(
                            R.string.pm10_measurement_string,
                            pollutionData.components.pm10.toString().take(6)
                        )
                        tvNH3Measurement.text = resources.getString(
                            R.string.nh3_measurement_string,
                            pollutionData.components.nh3.toString().take(6)
                        )
                    }
                }

                is Resource.Error -> {
                    parentLayout.isVisible = false
                    childLayout.isVisible = false
                    progressBar.isVisible = false
                    response.message?.let { message ->
                        Toast.makeText(
                            requireContext(),
                            "Unable to fetch air pollution data: $message",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }

                is Resource.Loading -> {
                    parentLayout.isVisible = false
                    childLayout.isVisible = false
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

                    viewModel.getRemoteData(lat, lon, apiKey)
                }
            }
        }
    }

    private fun convertTemperature(value: Double): String {
        val convertedTemperature = (value - 273.15).roundToInt()
        return convertedTemperature.toString()
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
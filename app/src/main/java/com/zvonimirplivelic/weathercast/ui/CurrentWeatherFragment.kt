package com.zvonimirplivelic.weathercast.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.zvonimirplivelic.weathercast.R
import com.zvonimirplivelic.weathercast.WeatherCastViewModel
import com.zvonimirplivelic.weathercast.util.Constants.LATITUDE
import com.zvonimirplivelic.weathercast.util.Constants.LONGITUDE
import com.zvonimirplivelic.weathercast.util.Resource
import timber.log.Timber

class CurrentWeatherFragment : Fragment() {

    private lateinit var viewModel: WeatherCastViewModel
    private lateinit var weatherDataTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_current_weather, container, false)

        val appInfo: ApplicationInfo = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
        val apiKey = appInfo.metaData["apiKey"].toString()

        viewModel = ViewModelProvider(this)[WeatherCastViewModel::class.java]

        progressBar = view.findViewById(R.id.progress_bar)
        weatherDataTextView = view.findViewById(R.id.tv_weather_data)

        viewModel.getWeatherData(LATITUDE, LONGITUDE, apiKey)

        viewModel.weatherData.observe(viewLifecycleOwner) { response ->
            when (response) {

                is Resource.Success -> {
                    progressBar.isVisible = false
                    response.data?.let { weatherResponse ->
                        Timber.d("ResponseMain: ${weatherResponse}")
                        weatherDataTextView.text =
                            "${weatherResponse.name}${weatherResponse.main.temp}"
                    }
                }

                is Resource.Error -> {
                    progressBar.isVisible = false
                    Timber.d("ResponseResErr: $response Resmes:${response.message}")
                    response.message?.let { message ->
                        Toast.makeText(requireContext(), "An error occured: $message", Toast.LENGTH_LONG)
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
}
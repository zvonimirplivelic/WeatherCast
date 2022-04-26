package com.zvonimirplivelic.weathercast

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.zvonimirplivelic.weathercast.model.WeatherResponse
import com.zvonimirplivelic.weathercast.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

class WeatherCastViewModel(
    app: Application
) : AndroidViewModel(app) {
    private val weatherCastRepository: WeatherCastRepository = WeatherCastRepository()

    val weatherData: MutableLiveData<Resource<WeatherResponse>> = MutableLiveData()
    var weatherDataResponse: WeatherResponse? = null


    fun getWeatherData(
        latitude: String,
        longitude: String,
        apiKey: String
    ) = viewModelScope.launch {
        safeWeatherDataCall(latitude, longitude, apiKey)
    }

    private suspend fun safeWeatherDataCall(latitude: String, longitude: String, apiKey: String) {
        weatherData.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = weatherCastRepository.getWeatherData(latitude, longitude, apiKey)
                Timber.d(
                    "URL request: ${response.raw().request.url}"
                )
                delay(444L)
                Timber.d("ResponseSWDC: ${response.errorBody()}")
                weatherData.postValue(handleWeatherDataResponse(response))
            } else {
                weatherData.postValue(Resource.Error("No internet connection"))
                Timber.d("VMError: $weatherData")
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> weatherData.postValue(Resource.Error("Network Failure"))
                else -> {
                    weatherData.postValue(Resource.Error("Conversion Error: $t"))
                    Timber.d("VMError lm ${t.localizedMessage}")
                    Timber.d("VMError st${t.stackTrace}")
                    Timber.d("VMError sup${t.suppressed}")
                    Timber.d("VMError cause${t.cause}")
                    Timber.d("VMError message${t.message}")
                }
            }

        }
    }

    private fun handleWeatherDataResponse(response: Response<WeatherResponse>): Resource<WeatherResponse> {
        Timber.d("ResponseHWDR: ${response.message()}")
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                weatherDataResponse = resultResponse

                return Resource.Success(weatherDataResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<WeatherCastApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return false
    }

}
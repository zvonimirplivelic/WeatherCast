package com.zvonimirplivelic.weathercast

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zvonimirplivelic.weathercast.model.AirPollutionResponse
import com.zvonimirplivelic.weathercast.model.DetailedWeatherResponse
import com.zvonimirplivelic.weathercast.model.WeatherResponse
import com.zvonimirplivelic.weathercast.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val detailedWeatherData: MutableLiveData<Resource<DetailedWeatherResponse>> = MutableLiveData()
    val airPollutionWeatherData: MutableLiveData<Resource<AirPollutionResponse>> = MutableLiveData()

    private var weatherDataResponse: WeatherResponse? = null
    var detailedWeatherResponse: DetailedWeatherResponse? = null
    var airPollutionResponse: AirPollutionResponse? = null

    fun getRemoteData(
        latitude: String,
        longitude: String,
        apiKey: String
    ) = viewModelScope.launch {
        coroutineScope {
            safeRemoteDataCall(latitude, longitude, apiKey)
        }
    }

    private suspend fun safeRemoteDataCall(latitude: String, longitude: String, apiKey: String) {
        weatherData.postValue(Resource.Loading())
        detailedWeatherData.postValue(Resource.Loading())
        airPollutionWeatherData.postValue(Resource.Loading())

        try {
            if (hasInternetConnection()) {
                coroutineScope {
                    val weatherDataResponse =
                        async { weatherCastRepository.getWeatherData(latitude, longitude, apiKey) }
                    val detailedDataResponse = async {
                        weatherCastRepository.getDetailedWeatherData(
                            latitude,
                            longitude,
                            apiKey
                        )
                    }
                    val airPollutionDataResponse =
                        async {
                            weatherCastRepository.getAriPollutionData(
                                latitude,
                                longitude,
                                apiKey
                            )
                        }
                    delay(444L)

                    weatherData.postValue(handleWeatherDataResponse(weatherDataResponse.await()))
                    detailedWeatherData.postValue(handleDetailedDataResponse(detailedDataResponse.await()))
                    airPollutionWeatherData.postValue(
                        handleAirPollutionDataResponse(
                            airPollutionDataResponse.await()
                        )
                    )
                }
            } else {
                weatherData.postValue(Resource.Error("No internet connection"))
                Timber.d("VMError: $weatherData")
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> weatherData.postValue(Resource.Error("Network Failure"))
                else -> {
                    weatherData.postValue(Resource.Error("Conversion Error: $t"))
                }
            }

        }
    }

    private fun handleWeatherDataResponse(response: Response<WeatherResponse>): Resource<WeatherResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                weatherDataResponse = resultResponse

                return Resource.Success(weatherDataResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleDetailedDataResponse(response: Response<DetailedWeatherResponse>): Resource<DetailedWeatherResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                detailedWeatherResponse = resultResponse

                return Resource.Success(detailedWeatherResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleAirPollutionDataResponse(response: Response<AirPollutionResponse>): Resource<AirPollutionResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                airPollutionResponse = resultResponse

                return Resource.Success(airPollutionResponse ?: resultResponse)
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
package com.zvonimirplivelic.weathercast

import com.zvonimirplivelic.weathercast.remote.RetrofitInstance

class WeatherCastRepository {

    suspend fun getWeatherData(
        latitude: String,
        longitude: String,
        apiKey: String
    ) = RetrofitInstance.api.getCurrentWeatherData(latitude, longitude, apiKey)
}
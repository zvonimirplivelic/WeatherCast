package com.zvonimirplivelic.weathercast

import com.zvonimirplivelic.weathercast.remote.RetrofitInstance

class WeatherCastRepository {

    suspend fun getWeatherData(
        latitude: String,
        longitude: String,
        apiKey: String
    ) = RetrofitInstance.api.getCurrentWeatherData(latitude, longitude, apiKey)

    suspend fun getDetailedWeatherData(
        latitude: String,
        longitude: String,
        apiKey: String
    ) = RetrofitInstance.api.getDetailedWeatherData(latitude, longitude, apiKey)

    suspend fun getAriPollutionData(
        latitude: String,
        longitude: String,
        apiKey: String
    ) = RetrofitInstance.api.getAirPollutionData(latitude, longitude, apiKey)


}
package com.zvonimirplivelic.weathercast.remote

import com.zvonimirplivelic.weathercast.model.AirPollutionResponse
import com.zvonimirplivelic.weathercast.model.DetailedWeatherResponse
import com.zvonimirplivelic.weathercast.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherCastService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherData(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("appid") apiKey: String
    ): Response<WeatherResponse>

    @GET("data/2.5/onecall")
    suspend fun getDetailedWeatherData(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("appid") apiKey: String
    ): Response<DetailedWeatherResponse>

    @GET("data/2.5/air_pollution")
    suspend fun getAirPollutionData(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("appid") apiKey: String
    ): Response<AirPollutionResponse>
}
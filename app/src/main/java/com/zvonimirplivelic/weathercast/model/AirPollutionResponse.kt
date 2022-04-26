package com.zvonimirplivelic.weathercast.model


import com.google.gson.annotations.SerializedName

data class AirPollutionResponse(
    @SerializedName("coord")
    val coord: Coord,
    @SerializedName("list")
    val list: List<>
) {
    data class Coord(
        @SerializedName("lat")
        val lat: Double,
        @SerializedName("lon")
        val lon: Int
    )

    data class (
        @SerializedName("components")
        val components: Components,
        @SerializedName("dt")
        val dt: Int,
        @SerializedName("main")
        val main: Main
    ) {
        data class Components(
            @SerializedName("co")
            val co: Double,
            @SerializedName("nh3")
            val nh3: Double,
            @SerializedName("no")
            val no: Double,
            @SerializedName("no2")
            val no2: Double,
            @SerializedName("o3")
            val o3: Double,
            @SerializedName("pm10")
            val pm10: Double,
            @SerializedName("pm2_5")
            val pm25: Double,
            @SerializedName("so2")
            val so2: Double
        )

        data class Main(
            @SerializedName("aqi")
            val aqi: Int
        )
    }
}
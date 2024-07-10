package com.example.mvvm_weather.network.dtos

import com.google.gson.annotations.SerializedName

data class WeatherIntervalsDto (
    @SerializedName("time")
    val times: List<String>,
    @SerializedName("temperature_2m")
    val temperatures: List<Double>,
    @SerializedName("wind_speed_10m")
    val windSpeeds: List<Double>,
    @SerializedName("relative_humidity_2m")
    val humidity: List<Double>
)
package com.example.mvvm_weather.network.dtos

import com.google.gson.annotations.SerializedName

data class WeatherScheduleDto(
    @SerializedName("hourly")
    val intervals: WeatherIntervalsDto
)
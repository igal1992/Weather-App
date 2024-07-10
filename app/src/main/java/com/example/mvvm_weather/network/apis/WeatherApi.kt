package com.example.mvvm_weather.network.apis

import com.example.mvvm_weather.network.dtos.WeatherScheduleDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("/v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") currentParams: String = "temperature_2m,wind_speed_10m",
        @Query("hourly") hourlyParams: String = "temperature_2m,relative_humidity_2m,wind_speed_10m"
    ): Response<WeatherScheduleDto>
}
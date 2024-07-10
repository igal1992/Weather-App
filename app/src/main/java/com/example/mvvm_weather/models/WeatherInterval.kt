package com.example.mvvm_weather.models

import java.util.Date

class WeatherInterval(
    val date: Date, val temperature: Double, val windSpeed: Double,
    val humidity: Double
)
package com.example.mvvm_weather.models

import java.util.Date

class WeatherInterval(date: Date, temperature: Double, windSpeed: Double, humidity:Double) {
    val date: Date = date
    val temperature: Double = temperature
    val windSpeed: Double = windSpeed
    val humidity: Double = humidity
}
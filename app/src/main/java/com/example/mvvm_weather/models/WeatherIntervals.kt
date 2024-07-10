package com.example.mvvm_weather.models

import com.example.mvvm_weather.network.dtos.WeatherScheduleDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherIntervals(weatherScheduleDto: WeatherScheduleDto) {
    val intervals: MutableList<WeatherInterval> = mutableListOf()

    init {
        for (index in 0 until weatherScheduleDto.intervals.times.size) {
            val date = convertStringToDate(weatherScheduleDto.intervals.times[index])
            val temperature = weatherScheduleDto.intervals.temperatures[index]
            val windSpeed = weatherScheduleDto.intervals.windSpeeds[index]
            val humidity = weatherScheduleDto.intervals.humidity[index]
            val weatherInterval = WeatherInterval(date, temperature, windSpeed, humidity)
            intervals.add(weatherInterval)
        }
    }
    fun convertStringToDate(dateString: String): Date {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        return format.parse(dateString) ?: Date()
    }
}
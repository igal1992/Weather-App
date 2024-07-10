package com.example.mvvm_weather.app_services

import android.util.Log
import com.example.mvvm_weather.BuildConfig
import com.example.mvvm_weather.network.apis.WeatherApi
import com.example.mvvm_weather.network.dtos.WeatherScheduleDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class WeatherAppService {
    private val weatherApi = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
    ): Response<WeatherScheduleDto>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApi.getWeather(latitude, longitude)
                if (response.isSuccessful) {
                    response
                } else {
                    // Handle unsuccessful response
                    Log.e(
                        "WeatherData",
                        "Response not successful: ${response.errorBody()?.string()}"
                    )
                    null
                }
            } catch (e: IOException) {
                // Handle network error
                Log.e("WeatherData", "Network error: ${e.message}", e)
                null
            } catch (e: HttpException) {
                // Handle HTTP error
                Log.e("WeatherData", "HTTP error: ${e.message}", e)
                null
            } catch (e: Exception) {
                // Handle other errors
                Log.e("WeatherData", "Unexpected error: ${e.message}", e)
                null
            }
        }
    }
}
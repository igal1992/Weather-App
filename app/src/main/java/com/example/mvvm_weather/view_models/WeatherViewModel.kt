package com.example.mvvm_weather.view_models

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mvvm_weather.models.WeatherIntervals
import com.example.mvvm_weather.app_services.WeatherAppService
import com.example.mvvm_weather.database.daos.LocationDao
import com.example.mvvm_weather.database.entities.LocationEntity
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherViewModel(
    private val weatherAppService: WeatherAppService,
    private val locationDao: LocationDao
) : ViewModel() {
    private val _lineData = MutableLiveData<LineData>()
    val lineData: LiveData<LineData> get() = _lineData

    private val _xValueFormatterData = MutableLiveData<IndexAxisValueFormatter>()
    val xValueFormatterData: LiveData<IndexAxisValueFormatter> get() = _xValueFormatterData

    private val _locationData = locationDao.getAllLocations().asLiveData()
    val locationEntityData: LiveData<List<LocationEntity>> get() = _locationData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading


    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    suspend fun fetchWeather(latitude: Double, longitude: Double) {
        _isLoading.postValue(true)
        val response = weatherAppService.getWeather(latitude, longitude)
        if (response != null) {
            if (response.isSuccessful) {
                _isLoading.postValue(false)
                val weatherData = WeatherIntervals(response.body()!!)
                val entriesTemperature = ArrayList<Entry>()
                val entriesWindSpeed = ArrayList<Entry>()
                val entriesHumidity = ArrayList<Entry>()

                for ((index, data) in weatherData.intervals.withIndex()) {
                    entriesTemperature.add(Entry(index.toFloat(), data.temperature.toFloat()))
                    entriesWindSpeed.add(Entry(index.toFloat(), data.windSpeed.toFloat()))
                    entriesHumidity.add(Entry(index.toFloat(), data.humidity.toFloat()))
                }

                val temperatureDataSet = LineDataSet(entriesTemperature, "Temperature").apply {
                    color = Color.RED
                    valueTextColor = Color.BLACK
                }

                val windSpeedDataSet = LineDataSet(entriesWindSpeed, "Wind Speed").apply {
                    color = Color.BLUE
                    valueTextColor = Color.BLACK
                }

                val humidityDataSet = LineDataSet(entriesHumidity, "Humidity").apply {
                    color = Color.GREEN
                    valueTextColor = Color.BLACK
                }

                val lineData = LineData(temperatureDataSet, windSpeedDataSet, humidityDataSet)
                val dateFormatHour = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dateFormatDate = SimpleDateFormat("MMM dd", Locale.getDefault())
                val xAxisValueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt()
                        if (index >= 0 && index < weatherData.intervals.size) {
                            val weatherDataItem = weatherData.intervals[index]
                            val hour = dateFormatHour.format(weatherDataItem.date)
                            val date = dateFormatDate.format(weatherDataItem.date)
                            return "$hour\n$date"
                        }
                        return value.toString()
                    }
                }

                _lineData.postValue(
                    lineData
                )
                _xValueFormatterData.postValue(
                    xAxisValueFormatter
                )
                return
            } else {
                _isLoading.postValue(false)
                _error.postValue("Error: ${response.message()}")
            }
        }
        _isLoading.postValue(false)
        _error.postValue("Error: Failed to fetch weather data")
    }

    private fun insert(locationEntity: LocationEntity) = viewModelScope.launch {
        locationDao.insert(locationEntity)
    }

    fun onFetchWeatherClicked(latitude: Double, longitude: Double) = viewModelScope.launch {
        fetchWeather(latitude, longitude)
        insert(LocationEntity(0, latitude, longitude))
    }
}

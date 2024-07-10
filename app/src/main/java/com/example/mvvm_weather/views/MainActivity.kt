package com.example.mvvm_weather.views

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mvvm_weather.utils.CustomXAxisRenderer
import com.example.mvvm_weather.R
import com.example.mvvm_weather.models.WeatherIntervals
import com.example.mvvm_weather.database.entities.LocationEntity
import com.example.mvvm_weather.view_models.WeatherViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.renderer.XAxisRenderer
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val weatherViewModel: WeatherViewModel by viewModel()

    private lateinit var latitudeET: EditText
    private lateinit var longitudeET: EditText
    private lateinit var submitBtn: Button
    private lateinit var weatherLineChart: LineChart
    private lateinit var shimmerViewContainer: ShimmerFrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        init()
    }

    private fun initViews() {
        latitudeET = findViewById(R.id.latitude_edit_text)
        longitudeET = findViewById(R.id.longitude_edit_text)
        submitBtn = findViewById(R.id.submit_area)
        weatherLineChart = findViewById(R.id.weather_line_chart)
        shimmerViewContainer = findViewById(R.id.shimmer_view_container)
    }

    private fun init() {
        initLineChart()
        setObservers()
        setListeners()
    }

    private fun setListeners() {
        submitBtn.setOnClickListener {
            val latitude = latitudeET.text.toString().toDoubleOrNull()
            val longitude = longitudeET.text.toString().toDoubleOrNull()
            if (latitude != null && longitude != null) {
                weatherViewModel.onFetchWeatherClicked(latitude, longitude)
            } else {
                Toast.makeText(this, "Please enter valid coordinates", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initLineChart() {
        weatherLineChart.setXAxisRenderer(
            CustomXAxisRenderer(
                weatherLineChart.viewPortHandler,
                weatherLineChart.xAxis,
                weatherLineChart.getTransformer(weatherLineChart.axisLeft.axisDependency)
            )
        )
        weatherLineChart.axisLeft.apply {
            setDrawGridLines(false)
        }
        weatherLineChart.axisRight.isEnabled = false
        weatherLineChart.description = Description().apply {
            text = "Weather Data"
            textColor = Color.BLACK
        }
        weatherLineChart.legend.apply {
            form = Legend.LegendForm.LINE
            textColor = Color.BLACK
        }
    }

    private fun setChartDataSet(data: LineData) {
        weatherLineChart.data = data
        weatherLineChart.visibility =
            if (data.dataSetCount > 0) View.VISIBLE else View.GONE
        weatherLineChart.invalidate()
    }

    private fun setValueFormatter(xValueFormatter: IndexAxisValueFormatter) {
        weatherLineChart.xAxis.apply {
            valueFormatter = xValueFormatter
            granularity = 1f
            setDrawGridLines(false)
            setDrawAxisLine(false)
            textSize = 10f
        }
    }

    private fun setObservers() {
        weatherViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                shimmerViewContainer.startShimmer()
                shimmerViewContainer.visibility = View.VISIBLE
            } else {
                shimmerViewContainer.stopShimmer()
                shimmerViewContainer.visibility = View.GONE
            }
        }
        weatherViewModel.locationEntityData.observe(this) { locationData ->
            lifecycleScope.launch {
                if (locationData.isNotEmpty()) {
                    val latitude = locationData.last().latitude
                    val longitude = locationData.last().longitude
                    setValues(latitude, longitude)
                    weatherViewModel.fetchWeather(latitude, longitude)
                }
            }
            weatherViewModel.locationEntityData.removeObservers(this)
        }

        weatherViewModel.lineData.observe(this) { weatherData ->
            setChartDataSet(weatherData)
        }

        weatherViewModel.xValueFormatterData.observe(this) { formatter ->
            setValueFormatter(formatter)
        }

        weatherViewModel.error.observe(this) { error ->
            weatherLineChart.visibility = View.GONE
            Toast.makeText(this, error ?: "An error occurred", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setValues(latitude: Double, longitude: Double) {
        latitudeET.setText(latitude.toString())
        longitudeET.setText(longitude.toString())
    }
}


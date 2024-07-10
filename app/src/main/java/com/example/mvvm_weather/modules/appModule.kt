package com.example.mvvm_weather.modules

import androidx.room.Room
import com.example.mvvm_weather.view_models.WeatherViewModel
import com.example.mvvm_weather.app_services.WeatherAppService
import com.example.mvvm_weather.database.room.LocationDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { WeatherAppService() }
    single {
        Room.databaseBuilder(
            androidContext(),
            LocationDatabase::class.java,
            "location_database"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<LocationDatabase>().locationDao() }
    viewModel { WeatherViewModel(get(), get()) }
}
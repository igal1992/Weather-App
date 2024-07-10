package com.example.mvvm_weather.database.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mvvm_weather.database.daos.LocationDao
import com.example.mvvm_weather.database.entities.LocationEntity

@Database(entities = [LocationEntity::class], version = 1, exportSchema = false)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}





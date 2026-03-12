package com.example.codexkosherfood.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScanHistoryEntity::class],
    version = 3,
    exportSchema = true,
)
abstract class KosherFoodDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
}

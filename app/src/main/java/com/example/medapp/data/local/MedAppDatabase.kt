package com.example.medapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DoseEntity::class, CaregiverEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MedAppDatabase : RoomDatabase() {
    abstract fun doseDao(): DoseDao
    abstract fun caregiverDao(): CaregiverDao
}

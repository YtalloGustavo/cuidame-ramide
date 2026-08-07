package com.example.medapp.di

import android.content.Context
import androidx.room.Room
import com.example.medapp.data.local.CaregiverDao
import com.example.medapp.data.local.DoseDao
import com.example.medapp.data.local.MedAppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MedAppDatabase {
        return Room.databaseBuilder(
            context,
            MedAppDatabase::class.java,
            "medapp.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideDoseDao(database: MedAppDatabase): DoseDao = database.doseDao()

    @Provides
    fun provideCaregiverDao(database: MedAppDatabase): CaregiverDao = database.caregiverDao()
}

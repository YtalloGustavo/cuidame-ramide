package com.example.medapp.di

import com.example.medapp.data.local.CaregiverDao
import com.example.medapp.data.local.toCaregiver
import com.example.medapp.data.local.toEntity
import com.example.medapp.ui.screens.Caregiver
import com.example.medapp.ui.theme.Blue
import com.example.medapp.ui.theme.BlueLight
import com.example.medapp.ui.theme.GreenLight
import com.example.medapp.ui.theme.GreenPrimary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaregiverRepository @Inject constructor(
    private val caregiverDao: CaregiverDao
) {
    suspend fun loadCaregivers(): List<Caregiver> {
        val entities = caregiverDao.getAllOnce()
        return entities.map { it.toCaregiver() }.ifEmpty { defaultCaregivers() }
    }

    suspend fun saveCaregivers(caregivers: List<Caregiver>) {
        caregiverDao.deleteAll()
        caregiverDao.insertAll(caregivers.map { it.toEntity() })
    }

    fun defaultCaregivers() = listOf(
        Caregiver("1", "Maria Silva", "Mãe", "+55 81 99999-0001", "MA", GreenLight, GreenPrimary, true),
        Caregiver("2", "Carlos Rocha", "Filho", "+55 81 99999-0002", "CR", BlueLight, Blue, true)
    )
}

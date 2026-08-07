package com.example.medapp.di

import com.example.medapp.data.local.DoseDao
import com.example.medapp.data.local.toDose
import com.example.medapp.data.local.toEntity
import com.example.medapp.ui.screens.Dose
import com.example.medapp.ui.screens.DoseStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoseRepository @Inject constructor(
    private val doseDao: DoseDao
) {
    suspend fun loadDoses(): List<Dose> {
        val entities = doseDao.getAllOnce()
        return entities.map { it.toDose() }.ifEmpty { defaultDoses() }
    }

    suspend fun saveDoses(doses: List<Dose>) {
        doseDao.deleteAll()
        doseDao.insertAll(doses.map { it.toEntity() })
    }

    fun defaultDoses() = listOf(
        Dose("1", "Dipirona 500mg", "1 comprimido", "08:00", DoseStatus.Tomado),
        Dose("2", "Omeprazol 20mg", "1 cápsula", "12:00", DoseStatus.Pendente),
        Dose("3", "Losartana 50mg", "1 comprimido", "20:00", DoseStatus.MaisTarde)
    )
}

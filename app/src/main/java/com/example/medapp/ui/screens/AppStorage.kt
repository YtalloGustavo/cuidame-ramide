package com.example.medapp.ui.screens

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.medapp.ui.theme.Blue
import com.example.medapp.ui.theme.BlueLight
import com.example.medapp.ui.theme.GreenLight
import com.example.medapp.ui.theme.GreenPrimary

private const val PREFS_NAME = "medapp_local_state"
private const val DOSES_KEY = "doses"
private const val CAREGIVERS_KEY = "caregivers"
private const val FIELD_SEPARATOR = "|"
private const val ITEM_SEPARATOR = "\n"

fun defaultDoses() = listOf(
    Dose("1", "Dipirona 500mg", "1 comprimido", "08:00", DoseStatus.Tomado),
    Dose("2", "Omeprazol 20mg", "1 cápsula", "12:00", DoseStatus.Pendente),
    Dose("3", "Losartana 50mg", "1 comprimido", "20:00", DoseStatus.MaisTarde)
)

fun defaultCaregivers() = listOf(
    Caregiver("1", "Maria Silva", "Mãe", "+55 81 99999-0001", "MA", GreenLight, GreenPrimary, true),
    Caregiver("2", "Carlos Rocha", "Filho", "+55 81 99999-0002", "CR", BlueLight, Blue, true)
)

fun loadDoses(context: Context): List<Dose> {
    return runCatching {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(DOSES_KEY, null)
        if (raw.isNullOrBlank()) return defaultDoses()

        raw.lines().mapNotNull { line ->
            val parts = line.split(FIELD_SEPARATOR)
            if (parts.size != 5) return@mapNotNull null
            Dose(
                id = parts[0],
                name = parts[1],
                doseDescription = parts[2],
                time = parts[3],
                status = DoseStatus.entries.firstOrNull { it.name == parts[4] } ?: DoseStatus.Pendente
            )
        }.ifEmpty { defaultDoses() }
    }.getOrElse { defaultDoses() }
}

fun saveDoses(context: Context, doses: List<Dose>) {
    val raw = doses.joinToString(ITEM_SEPARATOR) {
        listOf(it.id, it.name, it.doseDescription, it.time, it.status.name).joinToString(FIELD_SEPARATOR)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(DOSES_KEY, raw)
        .apply()
}

fun loadCaregivers(context: Context): List<Caregiver> {
    return runCatching {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(CAREGIVERS_KEY, null)
        if (raw.isNullOrBlank()) return defaultCaregivers()

        raw.lines().mapNotNull { line ->
            val parts = line.split(FIELD_SEPARATOR)
            if (parts.size != 8) return@mapNotNull null
            Caregiver(
                id = parts[0],
                name = parts[1],
                relationship = parts[2],
                phone = parts[3],
                initials = parts[4],
                avatarBg = Color(parts[5].toULongOrNull() ?: GreenLight.value),
                avatarFg = Color(parts[6].toULongOrNull() ?: GreenPrimary.value),
                isActive = parts[7].toBoolean()
            )
        }.ifEmpty { defaultCaregivers() }
    }.getOrElse { defaultCaregivers() }
}

fun saveCaregivers(context: Context, caregivers: List<Caregiver>) {
    val raw = caregivers.joinToString(ITEM_SEPARATOR) {
        listOf(
            it.id,
            it.name,
            it.relationship,
            it.phone,
            it.initials,
            it.avatarBg.value.toString(),
            it.avatarFg.value.toString(),
            it.isActive.toString()
        ).joinToString(FIELD_SEPARATOR)
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(CAREGIVERS_KEY, raw)
        .apply()
}

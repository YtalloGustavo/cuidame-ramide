package com.example.medapp.data.local

import androidx.compose.ui.graphics.Color
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.medapp.ui.screens.Caregiver

@Entity(tableName = "caregivers")
data class CaregiverEntity(
    @PrimaryKey val id: String,
    val name: String,
    val relationship: String,
    val phone: String,
    val initials: String,
    val avatarBgValue: Long,
    val avatarFgValue: Long,
    val isActive: Boolean
)

fun CaregiverEntity.toCaregiver(): Caregiver = Caregiver(
    id = id,
    name = name,
    relationship = relationship,
    phone = phone,
    initials = initials,
    avatarBg = Color(avatarBgValue.toULong()),
    avatarFg = Color(avatarFgValue.toULong()),
    isActive = isActive
)

fun Caregiver.toEntity(): CaregiverEntity = CaregiverEntity(
    id = id,
    name = name,
    relationship = relationship,
    phone = phone,
    initials = initials,
    avatarBgValue = avatarBg.value.toLong(),
    avatarFgValue = avatarFg.value.toLong(),
    isActive = isActive
)

@Dao
interface CaregiverDao {

    @Query("SELECT * FROM caregivers ORDER BY id")
    suspend fun getAllOnce(): List<CaregiverEntity>

    @Query("DELETE FROM caregivers")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(caregivers: List<CaregiverEntity>)

    @Query("DELETE FROM caregivers WHERE id = :id")
    suspend fun deleteById(id: String)
}

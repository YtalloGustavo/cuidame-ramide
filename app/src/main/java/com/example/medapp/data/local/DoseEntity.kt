package com.example.medapp.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.example.medapp.ui.screens.Dose
import com.example.medapp.ui.screens.DoseStatus

@Entity(tableName = "doses")
data class DoseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val doseDescription: String,
    val time: String,
    val status: String,
    val ean: String? = null,
    val laboratory: String? = null,
    val activeIngredient: String? = null,
    val anvisaRegistration: String? = null
)

fun DoseEntity.toDose(): Dose = Dose(
    id = id,
    name = name,
    doseDescription = doseDescription,
    time = time,
    status = runCatching { DoseStatus.valueOf(status) }.getOrDefault(DoseStatus.Pendente),
    ean = ean,
    laboratory = laboratory,
    activeIngredient = activeIngredient
)

fun Dose.toEntity(): DoseEntity = DoseEntity(
    id = id,
    name = name,
    doseDescription = doseDescription,
    time = time,
    status = status.name,
    ean = ean,
    laboratory = laboratory,
    activeIngredient = activeIngredient,
    anvisaRegistration = null
)

@Dao
interface DoseDao {

    @Query("SELECT * FROM doses ORDER BY id")
    suspend fun getAllOnce(): List<DoseEntity>

    @Query("DELETE FROM doses")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doses: List<DoseEntity>)

    @Query("DELETE FROM doses WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE doses SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}

package com.example.medapp.data

import android.util.Log
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicineRepository @Inject constructor(
    private val apiService: AnvisaApiService
) {

    suspend fun fetchByEan(ean: String): Result<MedicineInfo> {
        val apiResult = runCatching { apiService.findMedicineByEan(ean) }

        if (apiResult.isSuccess) {
            apiResult.getOrNull()?.let { return Result.success(it) }
        }

        val exception = apiResult.exceptionOrNull()
        val isNotFound = exception is HttpException && exception.code() == 404

        findInMockDatabase(ean)?.let { return Result.success(it) }

        return if (isNotFound) {
            Result.failure(NoSuchElementException("EAN $ean nao cadastrado"))
        } else {
            Log.w(TAG, "API lookup failed for EAN=$ean", exception)
            Result.failure(exception ?: IllegalStateException("Medicamento nao encontrado"))
        }
    }

    suspend fun registerMedicine(info: MedicineInfo): Result<MedicineInfo> {
        return runCatching { apiService.registerMedicine(info) }
    }

    suspend fun getAllMedicines(): Result<List<MedicineInfo>> {
        return runCatching { apiService.getAllMedicines() }
    }

    suspend fun fetchBula(ean: String): Result<BulaInfo> {
        return runCatching { apiService.fetchBula(ean) }
    }

    suspend fun getInteractions(activeIngredient: String): Result<List<InteractionInfo>> {
        return runCatching { apiService.getInteractions(activeIngredient) }
    }

    suspend fun searchByName(query: String): Result<List<MedicineInfo>> {
        return runCatching { apiService.searchByName(query) }
    }

    private fun findInMockDatabase(ean: String): MedicineInfo? = MOCK_MEDICINES[ean]

    companion object {
        private const val TAG = "MedicineRepository"

        private val MOCK_MEDICINES: Map<String, MedicineInfo> = mapOf(
            "7891000100011" to MedicineInfo(
                ean = "7891000100011",
                name = "Losartana Potassica 50mg",
                laboratory = "EMS",
                anvisaRegistration = "1.0800.0194.001-9",
                activeIngredient = "Losartana Potassica",
                category = "Anti-hipertensivo",
                presentation = "Comprimido revestido 50mg - embalagem com 30"
            ),
            "7891000200025" to MedicineInfo(
                ean = "7891000200025",
                name = "Dipirona Sodica 500mg",
                laboratory = "EMS",
                anvisaRegistration = "1.0985.0432.001-4",
                activeIngredient = "Dipirona Sodica",
                category = "Analgesico e antitermico",
                presentation = "Comprimido 500mg - embalagem com 20"
            ),
            "7891000300039" to MedicineInfo(
                ean = "7891000300039",
                name = "Omeprazol 20mg",
                laboratory = "EMS",
                anvisaRegistration = "1.1234.0567.001-2",
                activeIngredient = "Omeprazol",
                category = "Protetor gastrico",
                presentation = "Capsula 20mg - embalagem com 28"
            )
        )
    }
}

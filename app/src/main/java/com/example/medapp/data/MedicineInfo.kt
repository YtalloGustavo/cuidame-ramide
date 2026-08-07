package com.example.medapp.data

import com.google.gson.annotations.SerializedName

data class MedicineInfo(
    @SerializedName("ean") val ean: String,
    @SerializedName("nomeProduto") val name: String,
    @SerializedName("razaoSocial") val laboratory: String,
    @SerializedName("numeroRegistro") val anvisaRegistration: String,
    @SerializedName("principioAtivo") val activeIngredient: String,
    @SerializedName("categoria") val category: String,
    @SerializedName("bulaUrl") val packageInsertUrl: String? = null,
    @SerializedName("apresentacao") val presentation: String? = null
)

sealed interface MedicineSearchUiState {
    data object Idle : MedicineSearchUiState
    data object Loading : MedicineSearchUiState
    data class Success(val medicine: MedicineInfo) : MedicineSearchUiState
    data class NotFound(val ean: String) : MedicineSearchUiState
    data class Error(val message: String) : MedicineSearchUiState
}

fun isValidEan13(code: String): Boolean {
    if (code.length != 13 || !code.all { it.isDigit() }) return false
    val digits = code.map { it.digitToInt() }
    val evenSum = (0..10 step 2).sumOf { digits[it] }
    val oddSum = (1..11 step 2).sumOf { digits[it] }
    val sum = evenSum + oddSum * 3
    val expected = (10 - sum % 10) % 10
    return expected == digits[12]
}
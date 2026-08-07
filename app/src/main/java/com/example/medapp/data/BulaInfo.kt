package com.example.medapp.data

import com.google.gson.annotations.SerializedName

data class BulaInfo(
    @SerializedName("ean") val ean: String,
    @SerializedName("paraQueServe") val paraQueServe: String,
    @SerializedName("efeitosColaterais") val efeitosColaterais: List<String>,
    @SerializedName("atencao") val atencao: String,
    @SerializedName("infoPaciente") val infoPaciente: String,
    @SerializedName("interacoes") val interacoes: String,
    @SerializedName("interacoesAlimentares") val interacoesAlimentares: String
)

sealed interface BulaUiState {
    data object Loading : BulaUiState
    data class Success(val bula: BulaInfo) : BulaUiState
    data object NotAvailable : BulaUiState
    data class Error(val message: String) : BulaUiState
}

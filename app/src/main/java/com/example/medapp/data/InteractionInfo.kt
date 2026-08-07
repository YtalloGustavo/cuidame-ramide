package com.example.medapp.data

import com.google.gson.annotations.SerializedName

data class InteractionInfo(
    @SerializedName("principioAtivo") val principioAtivo: String,
    @SerializedName("interageCom") val interageCom: String,
    @SerializedName("severidade") val severidade: String,
    @SerializedName("descricao") val descricao: String
)

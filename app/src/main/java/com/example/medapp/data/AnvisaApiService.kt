package com.example.medapp.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AnvisaApiService {

    @GET("medicamento/{ean}")
    suspend fun findMedicineByEan(@Path("ean") ean: String): MedicineInfo

    @POST("medicamento")
    suspend fun registerMedicine(@Body medicine: MedicineInfo): MedicineInfo

    @GET("buscar")
    suspend fun searchByName(@Query("q") query: String): List<MedicineInfo>

    @GET("medicamentos")
    suspend fun getAllMedicines(): List<MedicineInfo>

    @GET("medicamento/{ean}/bula")
    suspend fun fetchBula(@Path("ean") ean: String): BulaInfo

    @GET("interacoes")
    suspend fun getInteractions(@Query("principioAtivo") activeIngredient: String): List<InteractionInfo>
}

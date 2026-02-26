package com.ivandev.proyectoveterinaria.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class MascotaAdopcionModel(
    @SerializedName("id_mascota_adopcion") val idMascota: String? = null,
    @SerializedName("id_raza") val idRaza: String,
    @SerializedName("id_especie") val idEspecie: String,
    @SerializedName("nombre_raza") val nombreRaza: String,
    @SerializedName("nombre_especie") val nombreEspecie: String,
    @SerializedName("nombre_mascota") val nombreMascota: String,
    @SerializedName("sexo") val sexo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("contacto") val contacto: String,
    @SerializedName("edad_estimada") val edadEstimada: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("foto") val fotoUrl: String
)

interface MascotaAdopcionApiService {

    @GET("api/adopciones/listar")
    suspend fun listarMascotas(): Response<List<MascotaAdopcionModel>>

    @POST("api/adopciones/guardar")
    suspend fun guardarMascota(
        @Body mascota: MascotaAdopcionModel
    ): Response<MascotaAdopcionModel>

    @PUT("api/adopciones/editar/{id}")
    suspend fun editarMascota(
        @Path("id") id: String,
        @Body detalles: MascotaAdopcionModel
    ): Response<MascotaAdopcionModel>

    @DELETE("api/adopciones/eliminar/{id}")
    suspend fun eliminarMascota(
        @Path("id") id: String
    ): Response<String>
}
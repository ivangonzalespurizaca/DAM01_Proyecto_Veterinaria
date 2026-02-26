package com.ivandev.proyectoveterinaria.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MascotaAdopcion(
    @SerializedName("id_mascota_adopcion")
    var idMascotaAdopcion: String = "",
    @SerializedName("id_raza")
    var idRaza: String = "",
    @SerializedName("nombre_raza")
    var nombreRaza: String = "",
    @SerializedName("nombre_especie")
    var nombreEspecie: String = "",
    @SerializedName("id_especie")
    var idEspecie: String = "",
    @SerializedName("nombre_mascota")
    var nombreMascota: String = "",
    @SerializedName("sexo")
    var sexo: String = "",
    @SerializedName("edad_estimada")
    var edadEstimada: String = "",
    @SerializedName("estado")
    var estado: String = "Disponible",
    @SerializedName("descripcion")
    var descripcion: String = "",
    @SerializedName("contacto")
    var contacto: String = "",
    @SerializedName("foto")
    var foto: String = ""
) : Parcelable
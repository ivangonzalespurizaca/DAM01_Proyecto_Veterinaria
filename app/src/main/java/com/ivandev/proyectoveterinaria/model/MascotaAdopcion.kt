package com.ivandev.proyectoveterinaria.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MascotaAdopcion(
    var idMascotaAdopcion: String = "",
    var idRaza: String = "",
    var nombreRaza: String = "",
    var nombreEspecie: String = "",
    var idEspecie: String = "",
    var nombreMascota: String = "",
    var sexo: String = "",
    var edadEstimada: String = "",
    var estado: String = "Disponible",
    var descripcion: String = "",
    var contacto: String = "",
    var foto: String = ""
) : Parcelable
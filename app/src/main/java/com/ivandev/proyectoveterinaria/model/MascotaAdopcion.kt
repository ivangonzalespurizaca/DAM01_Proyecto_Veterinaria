package com.ivandev.proyectoveterinaria.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MascotaAdopcion(
    var idMascotaAdopcion: String = "",
    var idRaza: String = "",
    var idEspecie: String = "",
    var nombreMascota: String = "",
    var sexo: String = "",
    var edadEstimada: String = "",
    var estado: String = "Disponible",
    var foto: String = ""
) : Parcelable
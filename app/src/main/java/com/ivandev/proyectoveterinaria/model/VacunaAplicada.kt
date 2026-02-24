package com.ivandev.proyectoveterinaria.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VacunaAplicada(
    var idVacunaAplicada: String = "",
    var idMascota: String = "",
    var idVacuna: String = "",
    var nombreVacuna: String = "",
    var fechaAplicacion: String = "",
    var fechaProgramada: String = "",
    var nroDosis: Int = 1,
    var observaciones: String = "",
    var estado: String = "Completada"
) : Parcelable
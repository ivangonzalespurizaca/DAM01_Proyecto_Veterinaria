package com.ivandev.proyectoveterinaria.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConsultaMedica(
    val idConsulta: String = "",
    val idMascota: String = "",
    val idVeterinario: String = "",
    val nombreVeterinario: String = "",
    val fechaConsulta: String = "",
    val pesoActual: Double = 0.0,
    val temperatura: Double = 0.0,
    val motivo: String = "",
    val diagnostico: String = "",
    val recomendaciones: String = "",

    // Campos de tratamiento (unificados)
    val tipoMedicamento: String = "",
    val nombreMedicamento: String = "",
    val dosis: String = "",
    val frecuencia: String = "",
    val duracion: String = "",
    val fechaInicio: String = ""
) : Parcelable
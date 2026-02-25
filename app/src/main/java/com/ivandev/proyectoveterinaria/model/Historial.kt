package com.ivandev.proyectoveterinaria.model

data class Historial(
    val idConsulta: String = "",
    val idMascota: String = "",
    val idVeterinario: String = "",
    val nombreVeterinario: String = "", // Así está en tu Firebase
    val motivo: String = "",
    val fechaConsulta: String = "",
    // Campos que llenaremos manualmente
    var nombreMascota: String = "Cargando...",
    var especieNombre: String = ""
)
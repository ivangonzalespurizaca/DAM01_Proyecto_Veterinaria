package com.ivandev.proyectoveterinaria.model

data class VeterinarioCompleto(
    val usuario: Usuario,
    val especialidad: String? = null,
    val colegiatura: String? = null
)
package com.ivandev.proyectoveterinaria.model

data class MascotaIntro(
    val idMascota: String? = null,
    val nombreMascota: String? = null,
    val nombreEspecie: String? = null,
    val nombreRaza: String? = null,
    val foto: String? = null,
    val idCliente: String? = null,
    val sexo: String? = null
) {
    constructor() : this(null, null, null, null, null, null, null)
}
package com.ivandev.proyectoveterinaria.model



import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ReporteConsulta(
    // Datos Principales
    var idConsulta: String? = null,
    var nombreMascota: String? = null,
    var especieMascota: String? = null,
    var fotoMascota: String? = null,
    var idMascota: String? = null,

    // Datos de la Consulta
    var fechaConsulta: String? = null,
    var motivo: String? = null,
    var diagnostico: String? = null,
    var pesoActual: Double? = null,
    var temperatura: Double? = null,
    var recomendaciones: String? = null,

    // Datos del Tratamiento / Medicamento
    var nombreMedicamento: String? = null,
    var tipoMedicamento: String? = null,
    var dosis: String? = null,
    var frecuencia: String? = null,
    var duracion: String? = null,
    var fechaInicio: String? = null,

    // Datos del Veterinario
    var idVeterinario: String? = null,
    var nombreVeterinario: String? = null,

    // Campo extra por estabilidad del sistema
    var stability: Any? = null
) {
    // Constructor vacío requerido por Firestore
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
}
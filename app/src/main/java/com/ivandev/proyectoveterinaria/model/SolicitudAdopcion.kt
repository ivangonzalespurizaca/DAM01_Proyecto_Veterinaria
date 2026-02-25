package com.ivandev.proyectoveterinaria.model

data class SolicitudAdopcion(
    var idSolicitud: String = "",
    var idMascota: String = "",
    var nombreMascota: String = "",
    var fotoMascota: String = "",
    var idCliente: String = "",
    var nombreCliente: String = "",
    var telefonoCliente: String = "",
    var mensajeVet: String = "",
    var fecha: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now(),
    var fechaEntrevista: String = "",
    var estado: String = "Pendiente"
)
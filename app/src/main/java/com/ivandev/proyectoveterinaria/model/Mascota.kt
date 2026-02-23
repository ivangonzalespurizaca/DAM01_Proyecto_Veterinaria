package com.ivandev.proyectoveterinaria.model

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class Mascota(
    var idMascota: String = "",
    var nombreMascota: String = "",
    var idEspecie: String = "",
    var idRaza: String = "",
    var idCliente: String = "",
    var sexo: String = "",
    var fechaNacimiento: String = "",
    var pesoInicial: Double = 0.0,
    var dniDueno: String = "",
    var foto: String = "",
    var codigoQr: String = ""
) : Parcelable
package com.ivandev.proyectoveterinaria.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

@Keep
data class Usuario (
    @SerializedName("id_usuario")
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @SerializedName("nombres_completo")
    @get:PropertyName("nombreCompleto")
    @set:PropertyName("nombreCompleto")
    var nombreCompleto: String = "",

    @get:PropertyName("dni")
    @set:PropertyName("dni")
    var dni: String = "",

    @get:PropertyName("celular")
    @set:PropertyName("celular")
    var celular: String = "",

    @get:PropertyName("correo")
    @set:PropertyName("correo")
    var correo: String = "",

    // Agregamos PropertyName aquí para asegurar que Firestore lea "foto" correctamente
    @get:PropertyName("foto")
    @set:PropertyName("foto")
    var foto: String? = null,

    @get:PropertyName("rol")
    @set:PropertyName("rol")
    var rol: String = "",

    @get:PropertyName("estado")
    @set:PropertyName("estado")
    var estado: String = "Activo"
)
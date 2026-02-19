package com.ivandev.proyectoveterinaria.model

import com.google.gson.annotations.SerializedName

data class Usuario (
    @SerializedName("id_usuario")
    var idUsuario: String = "",

    @SerializedName("nombres_completo")
    var nombresCompleto: String = "",

    var dni: String = "",
    var celular: String = "",
    var correo: String = "",
    var contrasenia: String = "",
    var foto: String? = null,
    var rol: String = ""
)
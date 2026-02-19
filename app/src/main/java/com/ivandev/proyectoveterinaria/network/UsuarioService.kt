package com.ivandev.proyectoveterinaria.network

import com.ivandev.proyectoveterinaria.model.Usuario
import retrofit2.Call
import retrofit2.http.GET

interface UsuarioService {
    @GET("api/usuarios")
    fun getUsuarios(): Call<List<Usuario>>
}
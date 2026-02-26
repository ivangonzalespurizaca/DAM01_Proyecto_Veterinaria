package com.ivandev.proyectoveterinaria.network
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Asegúrate de que esta URL sea la misma que probaste en Postman
    private const val BASE_URL = "https://api-rest-5b15.onrender.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Instancia para el Login/Usuarios
    val usuarioService: UsuarioService by lazy {
        retrofit.create(UsuarioService::class.java)
    }

    // NUEVA: Instancia para las mascotas en adopción
    val mascotaAdopcionService: MascotaAdopcionApiService by lazy {
        retrofit.create(MascotaAdopcionApiService::class.java)
    }
}
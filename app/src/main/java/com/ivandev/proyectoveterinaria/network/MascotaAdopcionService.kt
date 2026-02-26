package com.ivandev.proyectoveterinaria.network

import com.ivandev.proyectoveterinaria.model.MascotaAdopcion


interface MascotaAdopcionService {
    fun guardar(mascota: MascotaAdopcion?): MascotaAdopcion?
    fun editar(id: String?, detalles: MascotaAdopcion?): MascotaAdopcion?
    fun listar(): MutableList<MascotaAdopcion?>?
    fun eliminar(id: String?): Boolean
}
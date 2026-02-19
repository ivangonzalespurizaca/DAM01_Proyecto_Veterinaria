package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.ViewModel
import com.ivandev.proyectoveterinaria.model.Usuario

class RegistroClienteViewModel : ViewModel(){
    var usuarioProceso = Usuario()
    init{
        usuarioProceso.rol = "Cliente"
    }
}

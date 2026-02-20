package com.ivandev.proyectoveterinaria.interfaces

import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity

interface IFragmentoToolbar {
    val titulo: String
    val tipo: PanelPrincipalActivity.TipoToolbar
}
package com.ivandev.proyectoveterinaria.fragment.veterinario

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentInicioVeterinarioBinding
import com.ivandev.proyectoveterinaria.databinding.FragmentPerfilUsuarioBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class InicioVeterinarioFragment : Fragment(R.layout.fragment_inicio_veterinario), IFragmentoToolbar {
    override val titulo: String = "PANEL PRINCIPAL"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL
    private var _binding: FragmentInicioVeterinarioBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInicioVeterinarioBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
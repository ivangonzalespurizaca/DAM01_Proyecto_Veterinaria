package com.ivandev.proyectoveterinaria.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentPerfilUsuarioBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class PerfilUsuarioFragment : Fragment(R.layout.fragment_perfil_usuario), IFragmentoToolbar {
    override val titulo: String = "MI PERFIL"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PERFIL
    private var _binding: FragmentPerfilUsuarioBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPerfilUsuarioBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
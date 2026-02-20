package com.ivandev.proyectoveterinaria.fragment.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentInicioAdminBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class InicioAdminFragment : Fragment(R.layout.fragment_inicio_admin), IFragmentoToolbar {
    override val titulo: String = "PANEL PRINCIPAL"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentInicioAdminBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInicioAdminBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
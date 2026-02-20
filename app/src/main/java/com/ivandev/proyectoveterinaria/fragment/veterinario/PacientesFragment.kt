package com.ivandev.proyectoveterinaria.fragment.veterinario

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentPacientesBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class PacientesFragment : Fragment(R.layout.fragment_pacientes), IFragmentoToolbar {
    override val titulo: String = "PACIENTES"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL
    private var _binding: FragmentPacientesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPacientesBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
package com.ivandev.proyectoveterinaria.fragment.cliente

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentHistorialBinding
import com.ivandev.proyectoveterinaria.databinding.FragmentInicioAdminBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class HistorialFragment : Fragment(R.layout.fragment_historial), IFragmentoToolbar {
    override val titulo: String = "HISTORIAL CLINICO"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistorialBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
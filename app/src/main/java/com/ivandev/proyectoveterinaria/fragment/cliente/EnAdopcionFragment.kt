package com.ivandev.proyectoveterinaria.fragment.cliente

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentEnAdopcionBinding
import com.ivandev.proyectoveterinaria.databinding.FragmentInicioAdminBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class EnAdopcionFragment : Fragment(R.layout.fragment_en_adopcion), IFragmentoToolbar {
    override val titulo: String = "EN ADOPCIÓN"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentEnAdopcionBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEnAdopcionBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
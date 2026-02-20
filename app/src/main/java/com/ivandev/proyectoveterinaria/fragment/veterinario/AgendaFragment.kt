package com.ivandev.proyectoveterinaria.fragment.veterinario

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentAgendaBinding
import com.ivandev.proyectoveterinaria.databinding.FragmentConsultasBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class AgendaFragment : Fragment(R.layout.fragment_agenda), IFragmentoToolbar {
    override val titulo: String = "AGENDA"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL
    private var _binding: FragmentAgendaBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAgendaBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
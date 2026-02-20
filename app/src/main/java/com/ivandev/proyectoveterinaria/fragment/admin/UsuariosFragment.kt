package com.ivandev.proyectoveterinaria.fragment.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentInicioAdminBinding
import com.ivandev.proyectoveterinaria.databinding.FragmentUsuariosBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class UsuariosFragment : Fragment(R.layout.fragment_usuarios), IFragmentoToolbar {
    override val titulo: String = "USUARIOS"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentUsuariosBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsuariosBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
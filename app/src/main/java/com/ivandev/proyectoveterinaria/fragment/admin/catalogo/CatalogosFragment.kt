package com.ivandev.proyectoveterinaria.fragment.admin.catalogo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentCatalogosBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class CatalogosFragment : Fragment(R.layout.fragment_catalogos), IFragmentoToolbar {
    override val titulo: String = "CATÁLOGOS"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL
    private var _binding: FragmentCatalogosBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCatalogosBinding.bind(view)

        binding.cardAdopcion.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, MascotaAdopcionListadoFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardEspecies.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, EspeciesListadoFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardRazas.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, RazaListadoFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.cardVacunas.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, VacunasListadoFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
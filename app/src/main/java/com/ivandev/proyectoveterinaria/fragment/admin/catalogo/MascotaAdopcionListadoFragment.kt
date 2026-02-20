package com.ivandev.proyectoveterinaria.fragment.admin.catalogo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentCatalogosBinding
import com.ivandev.proyectoveterinaria.databinding.FragmentMascotaAdopcionListadoBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class MascotaAdopcionListadoFragment : Fragment(R.layout.fragment_mascota_adopcion_listado), IFragmentoToolbar {
    override val titulo: String = "MASCOTAS EN ADOPCIÓN"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO

    private var _binding: FragmentMascotaAdopcionListadoBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMascotaAdopcionListadoBinding.bind(view)

        binding.fabRegistrarMascota.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, RegistrarMascotaAdopcionFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
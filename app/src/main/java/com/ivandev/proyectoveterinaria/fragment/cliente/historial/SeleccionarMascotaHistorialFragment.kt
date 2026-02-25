package com.ivandev.proyectoveterinaria.fragment.cliente.historial

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.MisMascotasAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentSeleccionarMascotaHistorialBinding
import com.ivandev.proyectoveterinaria.fragment.cliente.historial.MiMascotaHistorialFragment
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Mascota
import com.ivandev.proyectoveterinaria.viewmodel.EspeciesViewModel
import com.ivandev.proyectoveterinaria.viewmodel.MascotaViewModel
import com.ivandev.proyectoveterinaria.viewmodel.RazasViewModel

class SeleccionarMascotaHistorialFragment : Fragment(R.layout.fragment_seleccionar_mascota_historial), IFragmentoToolbar {
    override val titulo: String = "HISTORIAL CLÍNICO"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentSeleccionarMascotaHistorialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MascotaViewModel by viewModels()
    private lateinit var adapter: MisMascotasAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSeleccionarMascotaHistorialBinding.bind(view)

        setupRecyclerView()
        observarMascotas()
    }

    private fun setupRecyclerView() {
        adapter = MisMascotasAdapter { mascota ->
            irAlHistorialDetallado(mascota)
        }
        binding.rvMascotasSeleccion.apply {
            adapter = this@SeleccionarMascotaHistorialFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observarMascotas() {
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        viewModel.listaMascotas.observe(viewLifecycleOwner) { lista ->
            // El adaptador ya sabe usar mascota.nombreRaza y mascota.nombreEspecie
            adapter.updateList(lista)
        }

        viewModel.cargarMascotasPorCliente(uidActual)
    }

    private fun irAlHistorialDetallado(mascota: Mascota) {
        val fragmentoDetalle = MiMascotaHistorialFragment().apply {
            arguments = Bundle().apply {
                // ENVIAR EL OBJETO COMPLETO: Así el segundo fragmento lo recibe sin problemas
                putParcelable("mascota", mascota)
            }
        }

        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.nav_host_fragment, fragmentoDetalle)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
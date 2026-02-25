package com.ivandev.proyectoveterinaria.fragment.cliente.enAdopcion

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.MisSolicitudesAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentMisSolicitudesBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.viewmodel.MascotaAdopcionViewModel

class MisSolicitudesFragment : Fragment(R.layout.fragment_mis_solicitudes), IFragmentoToolbar {

    override val titulo: String = "MIS SOLICITUDES"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentMisSolicitudesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MascotaAdopcionViewModel by viewModels()
    private lateinit var adapter: MisSolicitudesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMisSolicitudesBinding.bind(view)

        setupRecyclerView()
        observeViewModel()
        setupFiltros()

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (userId.isNotEmpty()) {
            viewModel.cargarSolicitudesParaCliente(userId)
        }


    }

    private fun setupRecyclerView() {
        adapter = MisSolicitudesAdapter(emptyList()) //
        binding.rvMisSolicitudes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@MisSolicitudesFragment.adapter
        }
    }

    private fun setupFiltros() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, checkedIds ->
            val estado = when (checkedIds.firstOrNull()) {
                R.id.chipPendientes -> "Pendiente"
                R.id.chipProceso -> "En Proceso"
                R.id.chipAprobadas -> "Aprobada"
                else -> "Todos"
            }
            viewModel.filtrarPorEstado(estado) //
        }
    }

    private fun observeViewModel() {
        viewModel.solicitudes.observe(viewLifecycleOwner) { lista ->
            // Manejo del "Empty State" (Punto extra de UX)
            if (lista.isEmpty()) {
                binding.layoutVacio.visibility = View.VISIBLE
                binding.rvMisSolicitudes.visibility = View.GONE
            } else {
                binding.layoutVacio.visibility = View.GONE
                binding.rvMisSolicitudes.visibility = View.VISIBLE
                adapter.actualizarLista(lista) //
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
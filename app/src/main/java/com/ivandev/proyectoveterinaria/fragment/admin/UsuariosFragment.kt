package com.ivandev.proyectoveterinaria.fragment.admin

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.PersonalAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentUsuariosBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Usuario
import com.ivandev.proyectoveterinaria.model.VeterinarioCompleto
import com.ivandev.proyectoveterinaria.viewmodel.PersonalViewModel

class UsuariosFragment : Fragment(R.layout.fragment_usuarios), IFragmentoToolbar {
    override val titulo: String = "GESTIONAR PERSONAL"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentUsuariosBinding? = null
    private val binding get() = _binding!!

    // Inyectamos el ViewModel
    private val viewModel: PersonalViewModel by viewModels()
    private lateinit var adapterPersonal: PersonalAdapter
    private var listaOriginal = listOf<VeterinarioCompleto>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUsuariosBinding.bind(view)

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        // Navegación para agregar nuevo personal
        binding.fabAgregarUsuario.setOnClickListener {
            // (activity as? PanelPrincipalActivity)?.replaceFragment(RegistrarVeterinarioFragment())
        }

        viewModel.cargarVeterinarios()
    }

    private fun setupRecyclerView() {
        adapterPersonal = PersonalAdapter(mutableListOf()){ usuario ->
            mostrarDialogoAnulacion(usuario)
        }
        binding.rvPersonal.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterPersonal
        }
    }

    // Lógica del buscador en tiempo real (sin botón)
    private fun setupSearch() {
        binding.etBuscarPersonal.addTextChangedListener { text ->
            val query = text.toString().lowercase()
            val listaFiltrada = listaOriginal.filter {
                it.usuario.nombreCompleto.lowercase().contains(query) || it.usuario.dni.contains(query)
            }
            adapterPersonal.actualizarLista(listaFiltrada)
        }
    }

    private fun mostrarDialogoAnulacion(usuario: Usuario) {
        val esInactivo = usuario.estado == "Inactivo"
        val titulo = if (esInactivo) "REACTIVAR CUENTA" else "ANULAR CUENTA"
        val mensaje = if (esInactivo) "¿Deseas habilitar de nuevo a ${usuario.nombreCompleto}?"
        else "¿Estás seguro de que deseas dar de baja a ${usuario.nombreCompleto}?"

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("SÍ, CONFIRMAR") { _, _ ->
                val nuevoEstado = if (esInactivo) "Activo" else "Inactivo"
                viewModel.cambiarEstadoCuenta(usuario.id, nuevoEstado) { exito ->
                    if (exito) {
                        viewModel.cargarVeterinarios()
                        val msg = if (esInactivo) "Cuenta reactivada" else "Cuenta anulada"
                        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.listaPersonal.observe(viewLifecycleOwner) { lista ->
            listaOriginal = lista
            adapterPersonal.actualizarLista(lista)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
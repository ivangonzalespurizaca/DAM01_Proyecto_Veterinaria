package com.ivandev.proyectoveterinaria.fragment.admin.usuario

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.PersonalAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentUsuariosBinding
import com.ivandev.proyectoveterinaria.fragment.admin.usuario.RegistrarVeterinarioFragment
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

        binding.fabAgregarUsuario.setOnClickListener {
            (activity as? PanelPrincipalActivity)?.replaceFragment(RegistrarVeterinarioFragment())
        }

        viewModel.cargarVeterinarios()
    }

    private fun setupRecyclerView() {
        adapterPersonal = PersonalAdapter(
            mutableListOf(),
            { usuario -> mostrarDialogoAnulacion(usuario) },
            { vet -> mostrarPerfilVeterinario(vet) }
        )
        binding.rvPersonal.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterPersonal
        }
    }

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

        AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("SÍ, CONFIRMAR") { _, _ ->
                val nuevoEstado = if (esInactivo) "Activo" else "Inactivo"
                viewModel.cambiarEstadoCuenta(usuario.id, nuevoEstado) { exito ->
                    if (exito) {
                        viewModel.cargarVeterinarios()
                        val msg = if (esInactivo) "Cuenta reactivada" else "Cuenta anulada"
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun mostrarPerfilVeterinario(vet: VeterinarioCompleto) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.layout_perfil_profesional, null)

        view.findViewById<TextView>(R.id.tvNombreDetalle).text = vet.usuario.nombreCompleto
        view.findViewById<TextView>(R.id.tvDniDetalle).text = vet.usuario.dni
        view.findViewById<TextView>(R.id.tvCelularDetalle).text = vet.usuario.celular
        view.findViewById<TextView>(R.id.tvCmvpDetalle).text = vet.colegiatura ?: "N/A"
        view.findViewById<TextView>(R.id.tvEspecialidadDetalle).text = vet.especialidad ?: "Médico General"
        view.findViewById<TextView>(R.id.tvSedeDetalle).text = vet.sede ?: "N/A"

        // Carga de foto con Glide
        val ivFoto = view.findViewById<ImageView>(R.id.ivFotoPerfilDetalle)
        Glide.with(this)
            .load(vet.usuario.foto)
            .circleCrop()
            .placeholder(R.drawable.ic_perfil_usuario)
            .error(R.drawable.ic_perfil_usuario)
            .fallback(R.drawable.ic_perfil_usuario)
            .into(ivFoto)

        dialog.setContentView(view)
        dialog.show()
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
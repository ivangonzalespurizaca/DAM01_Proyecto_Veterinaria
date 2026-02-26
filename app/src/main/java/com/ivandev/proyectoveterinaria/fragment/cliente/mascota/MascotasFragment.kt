package com.ivandev.proyectoveterinaria.fragment.cliente.mascota

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.MascotaAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentMascotasBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Mascota
import com.ivandev.proyectoveterinaria.room.DBHelper
import com.ivandev.proyectoveterinaria.viewmodel.EspeciesViewModel
import com.ivandev.proyectoveterinaria.viewmodel.MascotaViewModel
import com.ivandev.proyectoveterinaria.viewmodel.RazasViewModel

class MascotasFragment : Fragment(R.layout.fragment_mascotas), IFragmentoToolbar {

    override val titulo: String = "MIS MASCOTAS"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentMascotasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MascotaViewModel by viewModels()
    private val especieViewModel: EspeciesViewModel by viewModels()
    private val razaViewModel: RazasViewModel by viewModels()
    private lateinit var dbHelper: DBHelper
    private lateinit var mascotaAdapter: MascotaAdapter
    private var listaOriginal = listOf<Mascota>()
    private var especiesMap = mapOf<String, String>()
    private var razasMap = mapOf<String, String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dbHelper = DBHelper.getInstance(requireContext())
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMascotasBinding.bind(view)

        setupRecyclerView()
        setupSearch()
        cargarCatalogosYDatos()

        binding.fabAgregarMascotaClinica.setOnClickListener {
            abrirFormulario()
        }
    }

    private fun setupRecyclerView() {
        // Adaptamos el click listener para manejar las 3 opciones del menú
        mascotaAdapter = MascotaAdapter(mutableListOf()) { mascota, idOpcion ->
            when (idOpcion) {
                R.id.item_perfil -> abrirPerfil(mascota)
                R.id.item_carnet -> abrirVacunas(mascota) // Relacionado con tabla vacuna_aplicada
                R.id.item_modificar -> abrirFormulario(mascota)
            }
        }
        binding.rvMascotasClinica.apply {
            adapter = mascotaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun cargarCatalogosYDatos() {
        especieViewModel.cargarEspecies(dbHelper)
        razaViewModel.cargarRazas(dbHelper)

        especieViewModel.listaEspecies.observe(viewLifecycleOwner) { listaEsp ->
            especiesMap = listaEsp.associate { it.id to it.nombre }
            verificarCatalogosYDatos()
        }

        razaViewModel.listaRazas.observe(viewLifecycleOwner) { listaRaz ->
            razasMap = listaRaz.associate { it.id to it.nombre }
            verificarCatalogosYDatos()
        }
    }

    private fun verificarCatalogosYDatos() {
        // Garantizamos que los nombres de raza/especie estén listos antes de cargar mascotas
        if (especiesMap.isNotEmpty() && razasMap.isNotEmpty()) {
            observarMascotas()
        }
    }

    private fun observarMascotas() {
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        viewModel.listaMascotas.observe(viewLifecycleOwner) { lista ->
            listaOriginal = lista
            mascotaAdapter.actualizarMapas(especiesMap, razasMap)
            mascotaAdapter.actualizarLista(lista)
        }

        // Filtramos por el ID del cliente logueado
        viewModel.cargarMascotasPorCliente(uidActual)
    }

    private fun setupSearch() {
        binding.etBuscarMascotaClinica.addTextChangedListener { text ->
            val query = text.toString().lowercase().trim()
            val filtrada = listaOriginal.filter { mascota ->
                val nombreEspecie = especiesMap[mascota.idEspecie]?.lowercase() ?: ""
                val nombreRaza = razasMap[mascota.idRaza]?.lowercase() ?: ""

                // Búsqueda extendida: Nombre, Especie, Raza o DNI del dueño
                mascota.nombreMascota.lowercase().contains(query) ||
                        nombreEspecie.contains(query) ||
                        nombreRaza.contains(query) ||
                        mascota.dniDueno.contains(query)
            }
            mascotaAdapter.actualizarLista(filtrada)
        }
    }

    private fun abrirFormulario(mascota: Mascota? = null) {
        val fragment = RegistrarMascotaFragment().apply {
            arguments = Bundle().apply {
                putParcelable("mascota", mascota)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun abrirPerfil(mascota: Mascota) {
        val nombreEspecie = especiesMap[mascota.idEspecie] ?: "Desconocida"
        val nombreRaza = razasMap[mascota.idRaza] ?: "Desconocida"

        val bottomSheet = PerfilMascotaBottomSheet(mascota, nombreRaza, nombreEspecie)
        bottomSheet.show(parentFragmentManager, "PerfilMascota")
    }

    private fun abrirVacunas(mascota: Mascota) {
        val fragmentoCarnet = CarnetDeVacunasFragment()

        val bundle = Bundle().apply {
            putParcelable("mascota", mascota)
        }
        fragmentoCarnet.arguments = bundle

        // 3. Realizamos la navegació
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,  // Animación de entrada
                R.anim.slide_out_left
            )
            .replace(R.id.nav_host_fragment, fragmentoCarnet)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.ivandev.proyectoveterinaria.fragment.admin.catalogo

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.MascotasEnAdopcionAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentMascotaAdopcionListadoBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.MascotaAdopcion
import com.ivandev.proyectoveterinaria.viewmodel.EspeciesViewModel
import com.ivandev.proyectoveterinaria.viewmodel.MascotaAdopcionViewModel
import com.ivandev.proyectoveterinaria.viewmodel.RazasViewModel

class MascotaAdopcionListadoFragment : Fragment(R.layout.fragment_mascota_adopcion_listado), IFragmentoToolbar {

    override val titulo: String = "MASCOTAS EN ADOPCIÓN"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO

    private var _binding: FragmentMascotaAdopcionListadoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MascotaAdopcionViewModel by viewModels()
    private val especieViewModel: EspeciesViewModel by viewModels()
    private val razaViewModel: RazasViewModel by viewModels()

    private lateinit var mascotaAdapter: MascotasEnAdopcionAdapter
    private var listaOriginal = listOf<MascotaAdopcion>()
    private var especiesMap = mapOf<String, String>()
    private var razasMap = mapOf<String, String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMascotaAdopcionListadoBinding.bind(view)

        setupRecyclerView()
        setupSearch()
        cargarCatalogosYDatos()

        binding.fabAgregarMascota.setOnClickListener {
            abrirFormulario()
        }
    }

    private fun setupRecyclerView() {
        mascotaAdapter = MascotasEnAdopcionAdapter(mutableListOf()){ mascota ->
            abrirFormulario(mascota)
        }
        binding.rvMascotasAdopcion.apply {
            adapter = mascotaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun cargarCatalogosYDatos() {
        // 1. Disparamos la carga en los ViewModels
        especieViewModel.cargarEspecies()
        razaViewModel.cargarRazas()

        // 2. Observamos especies
        especieViewModel.listaEspecies.observe(viewLifecycleOwner) { listaEsp ->
            especiesMap = listaEsp.associate { it.id to it.nombre }
            verificarCatalogosYDatos()
        }

        // 3. Observamos razas
        razaViewModel.listaRazas.observe(viewLifecycleOwner) { listaRaz ->
            razasMap = listaRaz.associate { it.id to it.nombre }
            verificarCatalogosYDatos()
        }
    }

    // 4. Función de control para asegurar que tenemos los mapas antes de mostrar mascotas
    private fun verificarCatalogosYDatos() {
        // Solo cargamos mascotas si ya tenemos datos en los mapas (o si decides mostrarlos vacíos)
        if (especiesMap.isNotEmpty() && razasMap.isNotEmpty()) {
            observarMascotas()
        }
    }

    private fun observarMascotas() {
        // Importante: No vuelvas a crear el adaptador cada vez, solo actualiza la lista
        viewModel.listaMascotas.observe(viewLifecycleOwner) { lista ->
            listaOriginal = lista
            // Actualizamos los mapas en el adaptador actual si ya existe
            mascotaAdapter.actualizarMapas(especiesMap, razasMap)
            mascotaAdapter.actualizarLista(lista)
        }
        viewModel.cargarMascotas()
    }

    private fun setupSearch() {
        binding.etBuscarMascota.addTextChangedListener { text ->
            val query = text.toString().lowercase().trim()
            val filtrada = listaOriginal.filter { mascota ->
                val nombreEspecie = especiesMap[mascota.idEspecie]?.lowercase() ?: ""
                val nombreRaza = razasMap[mascota.idRaza]?.lowercase() ?: ""

                mascota.nombreMascota.lowercase().contains(query) ||
                        nombreEspecie.contains(query) ||
                        nombreRaza.contains(query)
            }
            mascotaAdapter.actualizarLista(filtrada)
        }
    }

    private fun abrirFormulario(mascota: MascotaAdopcion? = null) {
        val fragment = RegistrarMascotaAdopcionFragment().apply {
            arguments = Bundle().apply {
                putParcelable("mascota", mascota) // Gracias al @Parcelize
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
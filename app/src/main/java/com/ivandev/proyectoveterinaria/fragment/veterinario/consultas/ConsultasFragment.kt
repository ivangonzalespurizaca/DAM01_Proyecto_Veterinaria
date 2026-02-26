package com.ivandev.proyectoveterinaria.fragment.veterinario.consultas

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.MascotaBusquedaAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentConsultasBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Mascota
import com.ivandev.proyectoveterinaria.room.DBHelper
import com.ivandev.proyectoveterinaria.viewmodel.EspeciesViewModel
import com.ivandev.proyectoveterinaria.viewmodel.MascotaViewModel
import com.ivandev.proyectoveterinaria.viewmodel.RazasViewModel

class ConsultasFragment : Fragment(R.layout.fragment_consultas), IFragmentoToolbar {

    override val titulo: String = "GESTIONAR CONSULTAS"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentConsultasBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHelper

    // ViewModels necesarios para búsqueda y catálogos
    private val viewModel: MascotaViewModel by viewModels()
    private val especieViewModel: EspeciesViewModel by viewModels()
    private val razaViewModel: RazasViewModel by viewModels()

    private lateinit var adapterResultados: MascotaBusquedaAdapter
    private var especiesMap = mapOf<String, String>()
    private var razasMap = mapOf<String, String>()

    // 1. Lanzador del Escáner de QR
    private val qrLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            // El QR contiene el ID único, buscamos y vamos directo
            buscarYDecidir(result.contents, porQr = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dbHelper = DBHelper.getInstance(requireContext())
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentConsultasBinding.bind(view)

        setupRecyclerView()
        cargarCatalogos()

        // Listener para el botón de escaneo
        binding.btnEscanearQR.setOnClickListener {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Escanea el QR de la mascota")
                setBeepEnabled(true)
                setOrientationLocked(false)
            }
            qrLauncher.launch(options)
        }

        // Listener para búsqueda manual al presionar 'Enter'
        binding.etBuscarConsulta.setOnEditorActionListener { _, _, _ ->
            val query = binding.etBuscarConsulta.text.toString().trim()
            if (query.isNotEmpty()) {
                buscarYDecidir(query, porQr = false)
            }
            true
        }
    }

    private fun setupRecyclerView() {
        // Inicializamos con listas vacías, se actualizarán al cargar catálogos
        adapterResultados = MascotaBusquedaAdapter(mutableListOf(), emptyMap(), emptyMap()) { mascota ->
            irAPanelAcciones(mascota)
        }
        binding.rvResultadosBusqueda.apply {
            adapter = adapterResultados
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun cargarCatalogos() {
        especieViewModel.cargarEspecies(dbHelper)
        razaViewModel.cargarRazas(dbHelper)

        especieViewModel.listaEspecies.observe(viewLifecycleOwner) { especies ->
            especiesMap = especies.associate { it.id to it.nombre }
            actualizarAdaptador()
        }

        razaViewModel.listaRazas.observe(viewLifecycleOwner) { razas ->
            razasMap = razas.associate { it.id to it.nombre }
            actualizarAdaptador()
        }
    }

    private fun actualizarAdaptador() {
        // Re-inicializamos el adapter con los mapas cargados para ver nombres reales
        if (especiesMap.isNotEmpty() && razasMap.isNotEmpty()) {
            adapterResultados = MascotaBusquedaAdapter(mutableListOf(), especiesMap, razasMap) { mascota ->
                irAPanelAcciones(mascota)
            }
            binding.rvResultadosBusqueda.adapter = adapterResultados
        }
    }

    private fun buscarYDecidir(query: String, porQr: Boolean) {
        // Lógica de salto inteligente
        viewModel.buscarMascotasPorFiltroGeneral(query) { lista ->
            when {
                lista.isEmpty() -> Toast.makeText(context, "Mascota no encontrada", Toast.LENGTH_SHORT).show()

                // Si es QR o solo hay un resultado, saltamos directo al panel
                porQr || lista.size == 1 -> irAPanelAcciones(lista[0])

                // Si hay varios (ej: búsqueda por DNI), mostramos la lista
                else -> {
                    binding.tvLabelResultados.visibility = View.VISIBLE
                    binding.rvResultadosBusqueda.visibility = View.VISIBLE
                    adapterResultados.actualizarLista(lista)
                }
            }
        }
    }

    private fun irAPanelAcciones(mascota: Mascota) {
        // 1. Creamos el fragmento de destino manualmente
        val fragmentAcciones = AccionesConsultaFragment()

        // 2. Pasamos la mascota por Bundle
        val bundle = Bundle().apply {
            putParcelable("mascota", mascota)
        }
        fragmentAcciones.arguments = bundle

        // 3. Hacemos la transacción manual al contenedor genérico
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragmentAcciones) // Usamos tu ID actual
            .addToBackStack(null) // Para que el veterinario pueda regresar
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
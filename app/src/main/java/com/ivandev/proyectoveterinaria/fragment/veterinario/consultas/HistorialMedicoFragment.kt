package com.ivandev.proyectoveterinaria.fragment.veterinario.consultas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.BundleCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.ConsultaAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentHistorialMedicoBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.ConsultaMedica
import com.ivandev.proyectoveterinaria.model.Mascota
import com.ivandev.proyectoveterinaria.viewmodel.ConsultaViewModel

class HistorialMedicoFragment : Fragment(R.layout.fragment_historial_medico), IFragmentoToolbar {
    override val titulo: String = "HISTORIAL MÉDICO"
    override val tipo = PanelPrincipalActivity.TipoToolbar.SECUNDARIO
    private lateinit var binding: FragmentHistorialMedicoBinding
    private val viewModel: ConsultaViewModel by viewModels()
    private lateinit var consultaAdapter: ConsultaAdapter
    private var mascota: Mascota? = null
    private var listaOriginal = listOf<ConsultaMedica>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHistorialMedicoBinding.bind(view)

        // Recuperamos la mascota del bundle
        mascota = arguments?.let {
            BundleCompat.getParcelable(it, "mascota", Mascota::class.java)
        }

        setupRecyclerView()
        setupObservers()
        setupSearch()

        binding.fabNuevaConsulta.setOnClickListener {
            abrirRegistroConsulta()
        }

        // Carga inicial de datos
        mascota?.idMascota?.let { viewModel.listarConsultasPorMascota(it) }
    }

    private fun setupRecyclerView() {
        consultaAdapter = ConsultaAdapter { consulta ->
            mostrarDetallesConsultaDialog(consulta)
        }
        binding.rvConsultas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = consultaAdapter
        }
    }

    private fun setupObservers() {
        viewModel.listaConsultas.observe(viewLifecycleOwner) { lista ->
            listaOriginal = lista
            consultaAdapter.updateList(lista)
        }
    }

    private fun setupSearch() {
        // Usamos los 4 parámetros para que KTX no se confunda
        binding.etBuscarConsulta.doOnTextChanged { texto, _, _, _ ->
            val query = texto?.toString()?.lowercase() ?: ""

            // Filtramos sobre la listaOriginal que guardamos en el Observer
            val filtrada = listaOriginal.filter { it ->
                it.diagnostico.lowercase().contains(query) ||
                        it.motivo.lowercase().contains(query) ||
                        it.nombreMedicamento.lowercase().contains(query) // Búsqueda clínica integral
            }

            // Actualizamos el adaptador de consultas con el resultado filtrado
            consultaAdapter.updateList(filtrada)
        }
    }

    private fun mostrarDetallesConsultaDialog(consulta: ConsultaMedica) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_detalle_consulta_completa, null)

        // 1. Referencias de la sección clínica
        val tvFecha = dialogView.findViewById<TextView>(R.id.tvDetalleFecha)
        val tvMotivo = dialogView.findViewById<TextView>(R.id.tvDetalleMotivo)
        val tvVitals = dialogView.findViewById<TextView>(R.id.tvDetalleVitals)
        val tvDiagnostico = dialogView.findViewById<TextView>(R.id.tvDetalleDiagnostico)
        val tvRecomendaciones = dialogView.findViewById<TextView>(R.id.tvDetalleRecomendaciones)

        // 2. Referencias de la sección de tratamiento
        val layoutTratamiento = dialogView.findViewById<LinearLayout>(R.id.layoutDetalleTratamiento)
        val tvMedicamento = dialogView.findViewById<TextView>(R.id.tvDetalleMedicamento)
        val tvDosisFrecuencia = dialogView.findViewById<TextView>(R.id.tvDetalleDosisFrecuencia)
        val tvVeterinario = dialogView.findViewById<TextView>(R.id.tvDetalleVeterinario)

        // 3. Llenado de datos base
        tvFecha.text = "Consulta del: ${consulta.fechaConsulta}"
        tvMotivo.text = consulta.motivo
        tvVeterinario.text = "Atendido por: ${consulta.nombreVeterinario}"
        tvVitals.text = "Peso: ${consulta.pesoActual} kg  |  Temp: ${consulta.temperatura} °C"
        tvDiagnostico.text = consulta.diagnostico
        tvRecomendaciones.text = if (consulta.recomendaciones.isNullOrEmpty()) "Sin recomendaciones adicionales." else consulta.recomendaciones

        // 4. Lógica dinámica para el tratamiento
        if (consulta.nombreMedicamento.isNotEmpty()) {
            layoutTratamiento.visibility = View.VISIBLE
            tvMedicamento.text = "${consulta.tipoMedicamento}: ${consulta.nombreMedicamento}"
            tvDosisFrecuencia.text = "Dosis: ${consulta.dosis} | Frecuencia: ${consulta.frecuencia}"
        } else {
            layoutTratamiento.visibility = View.GONE
        }

        builder.setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun abrirRegistroConsulta() {
        val fragmento = RegistrarConsultaFragment()
        fragmento.arguments = Bundle().apply { putParcelable("mascota", mascota) }

        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragmento)
            .addToBackStack(null)
            .commit()
    }
}
package com.ivandev.proyectoveterinaria.fragment.veterinario.bitacora

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.BitacoraAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentBitacoraBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.ConsultaMedica
import com.ivandev.proyectoveterinaria.viewmodel.ConsultaViewModel
import java.text.SimpleDateFormat
import java.util.*

class BitacoraFragment : Fragment(R.layout.fragment_bitacora), IFragmentoToolbar {

    override val titulo: String = "BITÁCORA MÉDICA"
    override val tipo = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentBitacoraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConsultaViewModel by viewModels()
    private lateinit var bitacoraAdapter: BitacoraAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBitacoraBinding.bind(view)

        setupRecyclerView()
        setupDatePicker()
        setupObservers()

        // Recuperar ID del veterinario de SharedPreferences
        val prefs = requireActivity().getSharedPreferences("Sesion", Context.MODE_PRIVATE)
        val idVet = prefs.getString("idUsuario", "") ?: ""

        if (idVet.isNotEmpty()) {
            viewModel.listarConsultasPorVeterinario(idVet)
        }
    }

    private fun setupRecyclerView() {
        bitacoraAdapter = BitacoraAdapter { consulta ->
            mostrarDetallesConsultaDialog(consulta)
        }
        binding.rvBitacora.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bitacoraAdapter
        }
    }

    private fun setupObservers() {
        viewModel.listaConsultas.observe(viewLifecycleOwner) { lista ->
            bitacoraAdapter.updateList(lista)
        }
    }

    private fun setupDatePicker() {
        binding.etFiltroFecha.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccionar Fecha")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                // 1. Definimos el formato (el que usas en tu base de datos)
                val sdf = SimpleDateFormat("yyyy/dd/MM", Locale.getDefault())

                // 2. LA CLAVE: Forzamos a que use la zona horaria UTC
                sdf.timeZone = TimeZone.getTimeZone("UTC")

                // 3. Ahora sí, formateamos la fecha
                val fecha = sdf.format(Date(selection))

                binding.etFiltroFecha.setText(fecha)
                bitacoraAdapter.filtrar(fecha)
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        binding.tilFiltroFecha.setEndIconOnClickListener {
            binding.etFiltroFecha.text = null
            bitacoraAdapter.filtrar("")
        }
    }

    private fun mostrarDetallesConsultaDialog(consulta: ConsultaMedica) {
        val builder = MaterialAlertDialogBuilder(requireContext())

        // CAMBIO AQUÍ: Ahora inflamos el layout de la bitácora
        val dialogView = layoutInflater.inflate(R.layout.dialog_detalle_consulta_bitacora, null)

        // Referencias (Asegúrate de que los IDs existan en dialog_detalle_consulta_bitacora)
        val ivFoto = dialogView.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivDetalleFotoMascota)
        val tvMascota = dialogView.findViewById<TextView>(R.id.tvDetalleMascota)
        val tvFecha = dialogView.findViewById<TextView>(R.id.tvDetalleFecha)
        val tvMotivo = dialogView.findViewById<TextView>(R.id.tvDetalleMotivo)
        val tvPeso = dialogView.findViewById<TextView>(R.id.tvDetallePeso)
        val tvTemperatura = dialogView.findViewById<TextView>(R.id.tvDetalleTemp)
        val tvMedicamento = dialogView.findViewById<TextView>(R.id.tvDetalleMedicamento)
        val tvDosis = dialogView.findViewById<TextView>(R.id.tvDetalleDosisFrecuencia)

        // Carga de foto de Chester
        ivFoto?.let {
            Glide.with(dialogView.context)
                .load(consulta.fotoMascota)
                .placeholder(R.drawable.ic_pet)
                .into(it)
        }

        // Llenado de datos
        tvMascota?.text = consulta.nombreMascota
        tvFecha?.text = "Atención: ${consulta.fechaConsulta}"
        tvMotivo?.text = consulta.motivo
        tvPeso?.text = consulta.pesoActual.toString()
        tvTemperatura.text = consulta.temperatura.toString()
        tvMedicamento?.text = consulta.nombreMedicamento
        tvDosis?.text = consulta.dosis

        // Diagnóstico y recomendaciones
        dialogView.findViewById<TextView>(R.id.tvDetalleDiagnostico)?.text = consulta.diagnostico
        dialogView.findViewById<TextView>(R.id.tvDetalleRecomendaciones)?.text = consulta.recomendaciones

        builder.setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
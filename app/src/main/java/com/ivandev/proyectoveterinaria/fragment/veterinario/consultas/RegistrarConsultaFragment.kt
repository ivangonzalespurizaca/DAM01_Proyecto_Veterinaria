package com.ivandev.proyectoveterinaria.fragment.veterinario.consultas

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentRegistrarConsultaBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.ConsultaMedica
import com.ivandev.proyectoveterinaria.model.Mascota
import com.ivandev.proyectoveterinaria.viewmodel.ConsultaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RegistrarConsultaFragment : Fragment(R.layout.fragment_registrar_consulta), IFragmentoToolbar {
    override val titulo: String = "REGISTRAR CONSULTA"
    override val tipo = PanelPrincipalActivity.TipoToolbar.SECUNDARIO

    private var _binding: FragmentRegistrarConsultaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConsultaViewModel by viewModels()
    private var mascota: Mascota? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrarConsultaBinding.bind(view)

        // 1. Recuperamos la mascota del historial
        mascota = arguments?.let {
            BundleCompat.getParcelable(it, "mascota", Mascota::class.java)
        }

        setupUI()
    }

    private fun setupUI() {
        // Cargar tipos de medicamento en el ComboBox
        val tipos = resources.getStringArray(R.array.tipos_medicamento)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tipos)
        binding.actvTipoMedicamento.setAdapter(adapter)

        // Lógica del Switch para mostrar/ocultar tratamiento
        binding.switchIncluirTratamiento.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutCamposTratamiento.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Botón Guardar
        binding.btnGuardarConsulta.setOnClickListener {
            validarYGuardar()
        }
    }

    private fun obtenerDatosSesionVeterinario(): Pair<String, String> {
        val prefs = requireActivity().getSharedPreferences("Sesion", Context.MODE_PRIVATE)
        // Recuperamos el ID que guardamos en el MainActivity
        val idVet = prefs.getString("idUsuario", "") ?: ""
        val nombreVet = prefs.getString("nombreCompleto", "Veterinario de Guardia") ?: "Veterinario de Guardia"
        return Pair(idVet, nombreVet)
    }

    private fun validarYGuardar() {
        val motivo = binding.etMotivoConsulta.text.toString().trim()
        val diagnostico = binding.etDiagnostico.text.toString().trim()
        val peso = binding.etPesoActual.text.toString().toDoubleOrNull() ?: 0.0
        val temp = binding.etTemperatura.text.toString().toDoubleOrNull() ?: 0.0
        val dosis = binding.etDosisFrecuencia.text.toString()

        // Validación básica de campos obligatorios
        if (motivo.isEmpty() || diagnostico.isEmpty() || peso <= 0 || temp <= 0) {
            Toast.makeText(requireContext(), "Por favor, completa los datos clínicos básicos", Toast.LENGTH_SHORT).show()
            return
        }

        // Captura automática de fecha de hoy
        val (idVet, nombreVet) = obtenerDatosSesionVeterinario()
        val hoy = SimpleDateFormat("yyyy/dd/MM", Locale.getDefault()).format(Date())
        // Construcción del objeto consulta
        val consulta = ConsultaMedica(
            idConsulta = UUID.randomUUID().toString(),
            idMascota = mascota?.idMascota ?: "",
            fechaConsulta = hoy,
            dosis = dosis,
            idVeterinario = idVet,
            nombreVeterinario = nombreVet,
            pesoActual = peso,
            temperatura = temp,
            motivo = motivo,
            diagnostico = diagnostico,
            nombreMascota = mascota?.nombreMascota ?: "mascota desconocida",
            especieMascota = mascota?.nombreEspecie ?: "", // Usamos el nombre que ya traes
            fotoMascota = mascota?.foto ?: "",
            recomendaciones = binding.etRecomendaciones.text.toString(),
            // Datos de tratamiento si el switch está activo
            tipoMedicamento = if (binding.switchIncluirTratamiento.isChecked) binding.actvTipoMedicamento.text.toString() else "",
            nombreMedicamento = if (binding.switchIncluirTratamiento.isChecked) binding.etNombreMedicamento.text.toString() else "",
            frecuencia = if (binding.switchIncluirTratamiento.isChecked) binding.etDosisFrecuencia.text.toString() else "",
            duracion = if (binding.switchIncluirTratamiento.isChecked) binding.etDuracion.text.toString() else "",
            fechaInicio = if (binding.switchIncluirTratamiento.isChecked) hoy else ""
        )

        // Guardar en Firestore vía ViewModel
        viewModel.guardarConsulta(consulta) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "Consulta guardada exitosamente", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // Volver al historial
            } else {
                Toast.makeText(requireContext(), "Error al guardar. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
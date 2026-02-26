package com.ivandev.proyectoveterinaria.fragment.veterinario.consultas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentRegistrarAplicacionVacunaBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Mascota
import com.ivandev.proyectoveterinaria.model.VacunaAplicada
import com.ivandev.proyectoveterinaria.room.DBHelper
import com.ivandev.proyectoveterinaria.viewmodel.VacunaAplicadaViewModel
import com.ivandev.proyectoveterinaria.viewmodel.VacunasViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class RegistrarAplicacionVacunaFragment : Fragment(R.layout.fragment_registrar_aplicacion_vacuna), IFragmentoToolbar {
    override val titulo: String get() = if (vacunaAEditar != null) "EDITAR APLICACIÓN" else "REGISTRAR VACUNACIÓN"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO

    private lateinit var binding: FragmentRegistrarAplicacionVacunaBinding
    private val viewModel: VacunaAplicadaViewModel by viewModels()
    private val vacunaViewModel : VacunasViewModel by viewModels ()
    private var mascota: Mascota? = null
    private val calendar = Calendar.getInstance()
    private lateinit var dbHelper: DBHelper
    private var vacunaAEditar: VacunaAplicada? = null
    private val dateFormatter = SimpleDateFormat("yyyy/dd/MM", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRegistrarAplicacionVacunaBinding.bind(view)
        dbHelper = DBHelper.getInstance(requireContext())
        binding.switchEsProgramada.setOnCheckedChangeListener { _, isChecked ->
            actualizarVisibilidadFechas(isChecked)
        }

        mascota = arguments?.let {
            BundleCompat.getParcelable(it, "mascota", Mascota::class.java)
        }


        vacunaAEditar = arguments?.let {
            BundleCompat.getParcelable(it, "vacunaEditar", VacunaAplicada::class.java)
        }

        // 2. Si existe, llenamos el formulario
        vacunaAEditar?.let { cargarDatosParaEdicion(it) }


        if (mascota == null) {
            Toast.makeText(requireContext(), "Error al cargar datos del paciente", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        setupDatePickers()
        vacunaViewModel.cargarVacunas(dbHelper)
        setupCatalogObserver()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnGuardarVacuna.setOnClickListener {
            prepararYGuardar()
        }
    }

    private fun actualizarVisibilidadFechas(esProgramada: Boolean) {
        if (esProgramada) {
            // MODO CITA: Mostramos fecha programada, ocultamos fecha de aplicación
            binding.etFechaProgramada.visibility = View.VISIBLE
            binding.etFechaAplicacion.visibility = View.GONE
            binding.btnGuardarVacuna.text = "PROGRAMAR CITA"
        } else {
            // MODO APLICADA: Mostramos fecha de aplicación, ocultamos fecha programada
            binding.etFechaProgramada.visibility = View.GONE
            binding.etFechaAplicacion.visibility = View.VISIBLE
            binding.btnGuardarVacuna.text = "REGISTRAR APLICACIÓN"
        }
    }

    private fun setupCatalogObserver() {
        vacunaViewModel.listaVacunas.observe(viewLifecycleOwner) { listaDeVacunas ->
            // Extraemos solo los nombres para el ComboBox
            val nombresVacunas = listaDeVacunas.map { it.nombreVacuna }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                nombresVacunas
            )
            binding.actvNombreVacuna.setAdapter(adapter)
        }
    }

    private fun setupDatePickers() {
        // 1. Seteamos la fecha actual por defecto en ambos campos
        val hoy = dateFormatter.format(calendar.time)
        binding.etFechaAplicacion.setText(hoy)

        // 2. Deshabilitamos la escritura manual para obligar a usar el calendario
        // (También se puede hacer por XML con android:focusable="false")
        binding.etFechaAplicacion.isFocusable = false
        binding.etFechaProgramada.isFocusable = false

        // 3. Abrimos el diálogo al hacer clic
        binding.etFechaAplicacion.setOnClickListener { showDatePicker(binding.etFechaAplicacion) }
        binding.etFechaProgramada.setOnClickListener { showDatePicker(binding.etFechaProgramada) }
    }

    private fun showDatePicker(editText: android.widget.EditText) {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                editText.setText(dateFormatter.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun cargarDatosParaEdicion(v: VacunaAplicada) {
        binding.actvNombreVacuna.setText(v.nombreVacuna)
        binding.etNroDosis.setText(v.nroDosis.toString())
        binding.etObservacionesVacuna.setText(v.observaciones)

        // Configuramos el switch y las fechas según el estado guardado
        binding.switchEsProgramada.isChecked = (v.estado == "Pendiente")
        if (v.estado == "Aplicada") {
            binding.etFechaAplicacion.setText(v.fechaAplicacion)
        } else {
            binding.etFechaProgramada.setText(v.fechaProgramada)
        }

        binding.btnGuardarVacuna.text = "ACTUALIZAR REGISTRO"
    }

    private fun prepararYGuardar() {
        val nombre = binding.actvNombreVacuna.text.toString()
        val nroDosis = binding.etNroDosis.text.toString().toIntOrNull() ?: 1

        // REGLA DE NEGOCIO: Si el switch de "Programar a futuro" está activo
        val esProgramada = binding.switchEsProgramada.isChecked
        val estado = if (esProgramada) "Pendiente" else "Aplicada"

        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Elige una vacuna", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevaVacuna = VacunaAplicada(
            idVacunaAplicada = vacunaAEditar?.idVacunaAplicada ?: UUID.randomUUID().toString(),
            idMascota = mascota!!.idMascota, // Usamos el ID de la mascota recibida
            nombreVacuna = nombre,
            nroDosis = nroDosis,
            fechaAplicacion = if (!esProgramada) binding.etFechaAplicacion.text.toString() else "",
            fechaProgramada = if (esProgramada) binding.etFechaProgramada.text.toString() else "",
            estado = estado,
            observaciones = binding.etObservacionesVacuna.text.toString()
        )

        viewModel.guardarVacuna(nuevaVacuna) { exito ->
            if (exito) {
                val mensajeExito = if(vacunaAEditar == null){
                    "Vacuna registrada con éxito"
                } else{
                    "Registro actualizado correctamente"
                }

                Toast.makeText(requireContext(), mensajeExito, Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // Regresamos al historial
            } else {
                Toast.makeText(requireContext(), "Error al conectar con Firebase", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
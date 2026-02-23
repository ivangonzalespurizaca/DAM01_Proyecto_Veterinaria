package com.ivandev.proyectoveterinaria.fragment.veterinario.consultas

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.FragmentAccionesConsultaBinding
import com.ivandev.proyectoveterinaria.model.Mascota

class AccionesConsultaFragment : Fragment(R.layout.fragment_acciones_consulta) {

    private var _binding: FragmentAccionesConsultaBinding? = null
    private val binding get() = _binding!!
    private var mascota: Mascota? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccionesConsultaBinding.bind(view)

        // 1. RECUPERAR EL OBJETO: Aquí es donde "jalamos" los datos
        mascota = arguments?.getParcelable("mascota")

        // 2. LOG DE SEGURIDAD: Para ver en Logcat si llegó bien
        println("DEBUG: Mascota recibida -> ${mascota?.nombreMascota}")

        if (mascota != null) {
            setupUI(mascota!!)
            setupButtons()
        } else {
            Toast.makeText(context, "Error: No se cargaron los datos del paciente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI(m: Mascota) {
        // Accedemos al include 'cardMascotaSeleccionada' de tu XML
        binding.cardMascotaSeleccionada.apply {
            tvNombreBusqueda.text = m.nombreMascota
            tvRazaBusqueda.text = "Identificador: ${m.idMascota.takeLast(6).uppercase()}"

            Glide.with(requireContext())
                .load(m.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoBusqueda)
        }
    }

    private fun setupButtons() {
        // Dejamos listos los eventos para los 4 botones del grid
        binding.btnRegistrarVacuna.setOnClickListener {
            Toast.makeText(context, "Módulo Vacunas: ${mascota?.nombreMascota}", Toast.LENGTH_SHORT).show()
        }

        binding.btnTratamiento.setOnClickListener {
            // Aquí irá tu lógica de tratamientos
        }

        binding.btnRegistrarConsulta.setOnClickListener {
            // Aquí abriremos el formulario médico
        }

        binding.btnVerHistorial.setOnClickListener {
            // Aquí el historial clínico
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
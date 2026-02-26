package com.ivandev.proyectoveterinaria.fragment.veterinario

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentInicioVeterinarioBinding
import com.ivandev.proyectoveterinaria.databinding.FragmentPerfilUsuarioBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import java.util.Calendar

class InicioVeterinarioFragment : Fragment(R.layout.fragment_inicio_veterinario), IFragmentoToolbar {
    override val titulo: String = "PANEL PRINCIPAL"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL
    private var _binding: FragmentInicioVeterinarioBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInicioVeterinarioBinding.bind(view)
        setupDashboard()
    }

    private fun inicializarCalendarioConConsultas() {
        val db = FirebaseFirestore.getInstance()
        val calendar = Calendar.getInstance()

        // 1. Formatear la fecha de HOY para mostrarla al cargar
        val hoy = String.format("%04d/%02d/%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1)

        binding.tvFechaSeleccionada.text = "Hoy: $hoy"
        consultarFechaEnFirebase(hoy) // Carga inicial

        // 2. Listener cuando el usuario toca un día
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val fechaSeleccionada = String.format("%04d/%02d/%02d", year, dayOfMonth, month + 1)
            binding.tvFechaSeleccionada.text = "Consultas del $fechaSeleccionada"
            consultarFechaEnFirebase(fechaSeleccionada)
        }
    }

    private fun consultarFechaEnFirebase(fecha: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("consultas_medicas")
            .whereEqualTo("fechaConsulta", fecha)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener

                if (snapshots != null && !snapshots.isEmpty) {
                    // SI HAY CONSULTAS: Cambiamos el diseño para que resalte
                    binding.layoutInfoCitas.setBackgroundColor(Color.parseColor("#E3F2FD")) // Azul claro
                    val lista = snapshots.documents.joinToString("\n") {
                        "• ${it.getString("nombreMascota")} (${it.getString("motivo")})"
                    }
                    binding.tvResumenCitas.text = lista
                    binding.tvResumenCitas.setTextColor(Color.parseColor("#007AFF"))
                } else {
                    // NO HAY CONSULTAS
                    binding.layoutInfoCitas.setBackgroundColor(Color.parseColor("#F8F9FA"))
                    binding.tvResumenCitas.text = "Libre: No hay pacientes agendados."
                    binding.tvResumenCitas.setTextColor(Color.GRAY)
                }
            }
    }

    private fun setupDashboard() {
        val db = FirebaseFirestore.getInstance()

        // 1. Llenar los cuadros superiores (Métricas totales)
        db.collection("consultas_medicas").addSnapshotListener { snap, _ ->
            binding.tvCountAtenciones.text = snap?.size()?.toString() ?: "0"
        }

        db.collection("solicitudes_adopcion").whereEqualTo("estado", "Pendiente")
            .addSnapshotListener { snap, _ ->
                binding.tvCountPendientes.text = snap?.size()?.toString() ?: "0"
            }

        // 2. Lógica del Calendario
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Formatear fecha para que coincida con tu base de datos (Ej: 2026/25/02)
            val mes = String.format("%02d", month + 1)
            val dia = String.format("%02d", dayOfMonth)
            val fechaSeleccionada = "$year/$dia/$mes"



            // Buscar en Firebase por el campo 'fechaConsulta'
            db.collection("consultas_medicas")
                .whereEqualTo("fechaConsulta", fechaSeleccionada)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        val listaPacientes = docs.map { it.getString("nombreMascota") ?: "Mascota" }
                        binding.tvResumenCitas.text = "🐾 Pacientes: ${listaPacientes.joinToString(", ")}"
                        binding.tvResumenCitas.setTextColor(Color.BLACK)
                    } else {
                        binding.tvResumenCitas.text = "No hay consultas para este día."
                        binding.tvResumenCitas.setTextColor(Color.GRAY)
                    }
                }
        }
    }


}
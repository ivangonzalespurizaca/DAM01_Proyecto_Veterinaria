package com.ivandev.proyectoveterinaria.fragment.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.HistorialAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentInicioAdminBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Historial
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InicioAdminFragment : Fragment(R.layout.fragment_inicio_admin), IFragmentoToolbar {
    override val titulo: String = "PANEL PRINCIPAL"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentInicioAdminBinding? = null
    private val binding get() = _binding!!


    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var historialAdapter: HistorialAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInicioAdminBinding.bind(view)

        configurarFecha()
        cargarDatosAdmin()
        cargarEstadisticas()
        cargarHistorialConEspecies()
        setupRecyclerView()
    }

    private fun configurarFecha() {
        // Formato: "Hoy, 25 Feb"
        val sdf = SimpleDateFormat("'Hoy, 'dd MMM", Locale.getDefault())
        val fechaActual = sdf.format(Date())
        binding.tvCurrentDate.text = fechaActual
    }

    private fun cargarDatosAdmin() {
        val userId = auth.currentUser?.uid ?: return

        // Buscamos en la colección "usuarios" (o como se llame en tu Firebase)
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val nombre = document.getString("nombreCompleto") ?: "Admin"
                    binding.tvAdminName.text = "Adm. $nombre"
                }
            }
    }

    private fun cargarEstadisticas() {
        // 1. Contar Veterinarios
        db.collection("usuarios")
            .whereEqualTo("rol", "Veterinario") // Ajusta según tu lógica de roles
            .get()
            .addOnSuccessListener { snapshot ->
                val totalVetes = snapshot.size()
                binding.txtVete.text = "$totalVetes Activos"
            }

        // 2. Contar Mascotas
        db.collection("mascotas").get()
            .addOnSuccessListener { snapshot ->
                val totalMascotas = snapshot.size()
                 binding.txtMascotas.text = "$totalMascotas Reg."
            }

        db.collection("vacunas").get()
            .addOnSuccessListener { snapshot ->
                val totalvacunas = snapshot.size()
                binding.tvvacuna1.text = "$totalvacunas"
            }

        db.collection("vacunas_aplicadas").get()
            .addOnSuccessListener { snapshot ->
                val totalvacunasap = snapshot.size()
                binding.tvCount2.text = "+$totalvacunasap"
            }

        db.collection("solicitudes_adopcion")
            .whereEqualTo("estado", "Pendiente") // Ajusta según tu lógica de roles
            .get()
            .addOnSuccessListener { snapshot ->
                val pendientes = snapshot.size()
                binding.txtPendientes.text = "$pendientes NUEVAS"
            }





    }


    private fun cargarHistorialConEspecies() {
        db.collection("consultas_medicas")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val listaProvisional = snapshot?.toObjects(Historial::class.java) ?: emptyList()

                listaProvisional.forEach { historial ->
                    // 1. Buscamos la Mascota para obtener su nombre e idEspecie
                    if (historial.idMascota.isNotEmpty()) {
                        db.collection("mascotas").document(historial.idMascota).get()
                            .addOnSuccessListener { docMascota ->
                                historial.nombreMascota = docMascota.getString("nombreMascota") ?: "Mascota"
                                val especieIdDeMascota = docMascota.getString("idEspecie") ?: ""

                                // 2. Ahora que tenemos el idEspecie, buscamos su nombre
                                if (especieIdDeMascota.isNotEmpty()) {
                                    db.collection("especies").document(especieIdDeMascota).get()
                                        .addOnSuccessListener { docEspecie ->
                                            historial.especieNombre = docEspecie.getString("nombre") ?: ""

                                            // 3. Notificamos al adapter
                                            historialAdapter.updateList(listaProvisional)
                                        }
                                } else {
                                    historialAdapter.updateList(listaProvisional)
                                }
                            }
                    }
                }
            }
    }

    private fun setupRecyclerView() {
        historialAdapter = HistorialAdapter(emptyList())
        binding.rvConsultas.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = historialAdapter
        }}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




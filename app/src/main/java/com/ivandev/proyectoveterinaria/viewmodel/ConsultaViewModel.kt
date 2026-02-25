package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.model.ConsultaMedica
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ConsultaViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _listaConsultas = MutableLiveData<List<ConsultaMedica>>()
    val listaConsultas: LiveData<List<ConsultaMedica>> = _listaConsultas

    private val formatoFecha = SimpleDateFormat("yyyy/dd/MM", Locale.getDefault())

    fun listarConsultasPorMascota(idMascota: String) {
        db.collection("consultas_medicas")
            .whereEqualTo("idMascota", idMascota)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val todas = snapshot.toObjects(ConsultaMedica::class.java)

                    // Ordenamos de más reciente a más antigua
                    val listaOrdenada = todas.sortedByDescending { consulta ->
                        try {
                            formatoFecha.parse(consulta.fechaConsulta)
                        } catch (e: Exception) {
                            Date(0) // Si hay error, lo manda al final
                        }
                    }
                    _listaConsultas.value = listaOrdenada
                }
            }
    }

    fun listarConsultasPorVeterinario(idVeterinario: String) {
        db.collection("consultas_medicas")
            .whereEqualTo("idVeterinario", idVeterinario) // Filtro por médico
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    val todas = snapshot.toObjects(ConsultaMedica::class.java)

                    // Reutilizamos tu lógica de ordenamiento cronológico
                    val listaOrdenada = todas.sortedByDescending { consulta ->
                        try {
                            formatoFecha.parse(consulta.fechaConsulta)
                        } catch (e: Exception) {
                            Date(0)
                        }
                    }
                    _listaConsultas.value = listaOrdenada
                }
            }
    }

    fun guardarConsulta(consulta: ConsultaMedica, callback: (Boolean) -> Unit) {
        db.collection("consultas_medicas")
            .document(consulta.idConsulta)
            .set(consulta)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
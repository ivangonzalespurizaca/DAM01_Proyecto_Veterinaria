package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.model.VacunaAplicada
import java.text.SimpleDateFormat
import java.util.*

class VacunaAplicadaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _listaHistorial = MutableLiveData<List<VacunaAplicada>>()
    val listaHistorial: LiveData<List<VacunaAplicada>> = _listaHistorial
    private val _proximaVacuna = MutableLiveData<VacunaAplicada?>()
    private val formatoFecha = SimpleDateFormat("yyyy/dd/MM", Locale.getDefault())
    val proximaVacuna: LiveData<VacunaAplicada?> = _proximaVacuna

    // Método para GUARDAR o ACTUALIZAR
    fun guardarVacuna(vacuna: VacunaAplicada, callback: (Boolean) -> Unit) {
        db.collection("vacunas_aplicadas")
            .document(vacuna.idVacunaAplicada)
            .set(vacuna)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun listarVacunasPorMascota(idMascota: String) {
        db.collection("vacunas_aplicadas")
            .whereEqualTo("idMascota", idMascota)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val todas = snapshot.toObjects(VacunaAplicada::class.java)

                    // Ordenamos toda la lista de forma descendente (lo más nuevo o futuro arriba)
                    val listaCombinada = todas.sortedByDescending { vacuna ->
                        // Elegimos la fecha correcta según el estado para poder comparar
                        val fechaAComparar = if (vacuna.estado == "Aplicada") {
                            vacuna.fechaAplicacion
                        } else {
                            vacuna.fechaProgramada
                        }

                        try {
                            formatoFecha.parse(fechaAComparar)
                        } catch (e: Exception) {
                            Date(0) // En caso de error, lo manda al final
                        }
                    }

                    // 1. Actualizamos el historial completo
                    _listaHistorial.value = listaCombinada

                    // 2. Opcional: Seguimos guardando la "Próxima Vacuna" para el recordatorio destacado
                    _proximaVacuna.value = todas
                        .filter { it.estado == "Pendiente" }
                        .minByOrNull {
                            try { formatoFecha.parse(it.fechaProgramada) } catch (e: Exception) { Date(Long.MAX_VALUE) }
                        }
                }
            }
    }

    fun eliminarVacuna(vacuna: VacunaAplicada, callback: (Boolean) -> Unit) {
        if (vacuna.estado == "Pendiente") {
            db.collection("vacunas_aplicadas")
                .document(vacuna.idVacunaAplicada)
                .delete()
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { callback(false) }
        } else {
            // No permitimos eliminar si ya está aplicada
            callback(false)
        }
    }
}
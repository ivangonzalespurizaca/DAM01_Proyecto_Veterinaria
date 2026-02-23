package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.model.Vacuna

class VacunasViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _listaVacunas = MutableLiveData<List<Vacuna>>()
    val listaVacunas: LiveData<List<Vacuna>> get() = _listaVacunas

    fun cargarVacunas() {
        firestore.collection("vacunas")
            .get()
            .addOnSuccessListener { snapshot ->
                _listaVacunas.value = snapshot.toObjects(Vacuna::class.java)
            }
    }

    fun guardarVacuna(vacuna: Vacuna, onResult: (Boolean) -> Unit) {
        val db = firestore.collection("vacunas")
        val doc = if (vacuna.idVacuna.isEmpty()) db.document() else db.document(vacuna.idVacuna)
        val vacunaFinal = if (vacuna.idVacuna.isEmpty()) vacuna.copy(idVacuna = doc.id) else vacuna

        doc.set(vacunaFinal)
            .addOnSuccessListener { cargarVacunas(); onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun eliminarVacuna(id: String, onResult: (Boolean, String?) -> Unit) {
        // Validación de integridad: ¿Se ha aplicado esta vacuna antes?
        firestore.collection("vacuna_aplicada").whereEqualTo("id_vacuna", id).limit(1).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    firestore.collection("vacunas").document(id).delete()
                        .addOnSuccessListener { cargarVacunas(); onResult(true, null) }
                } else {
                    onResult(false, "No se puede eliminar: Esta vacuna ya figura en historiales médicos.")
                }
            }
    }
}
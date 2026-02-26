package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.model.Vacuna
import com.ivandev.proyectoveterinaria.room.DBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VacunasViewModel() : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _listaVacunas = MutableLiveData<List<Vacuna>>()
    val listaVacunas: LiveData<List<Vacuna>> get() = _listaVacunas

    fun cargarVacunas(dbLocal: DBHelper) {
        firestore.collection("vacunas").get()
            .addOnSuccessListener { snapshot ->
                val listaRemota = snapshot.toObjects(Vacuna::class.java)
                _listaVacunas.value = listaRemota

                viewModelScope.launch(Dispatchers.IO) {
                    listaRemota.forEach { dbLocal.insertarVacuna(it) }
                }
            }
            .addOnFailureListener {
                viewModelScope.launch(Dispatchers.IO) {
                    val listaLocal = dbLocal.listarVacunas()
                    withContext(Dispatchers.Main) {
                        _listaVacunas.value = listaLocal
                    }
                }
            }
    }

    fun guardarVacuna(vacuna: Vacuna, dbLocal: DBHelper, onResult: (Boolean) -> Unit) {
        val db = firestore.collection("vacunas")
        val doc = if (vacuna.idVacuna.isEmpty()) db.document() else db.document(vacuna.idVacuna)
        val vacunaFinal = if (vacuna.idVacuna.isEmpty()) vacuna.copy(idVacuna = doc.id) else vacuna

        doc.set(vacunaFinal)
            .addOnSuccessListener {
                viewModelScope.launch(Dispatchers.IO) {
                    dbLocal.insertarVacuna(vacunaFinal)
                    withContext(Dispatchers.Main) {
                        cargarVacunas(dbLocal)
                        onResult(true)
                    }
                }
            }
            .addOnFailureListener { onResult(false) }
    }

    // 1. Agregamos dbLocal como parámetro aquí también
    fun eliminarVacuna(id: String, dbLocal: DBHelper, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("vacuna_aplicada").whereEqualTo("id_vacuna", id).limit(1).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    firestore.collection("vacunas").document(id).delete()
                        .addOnSuccessListener {
                            viewModelScope.launch(Dispatchers.IO) {
                                dbLocal.eliminarVacuna(id)
                                withContext(Dispatchers.Main) {
                                    cargarVacunas(dbLocal)
                                    onResult(true, null)
                                }
                            }
                        }
                } else {
                    onResult(false, "No se puede eliminar: Esta vacuna ya figura en historiales médicos.")
                }
            }
    }
}
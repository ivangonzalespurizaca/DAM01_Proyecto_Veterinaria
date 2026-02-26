package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.model.Raza
import com.ivandev.proyectoveterinaria.room.DBHelper

class RazasViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _listaRazas = MutableLiveData<List<Raza>>()
    val listaRazas: LiveData<List<Raza>> get() = _listaRazas

    fun cargarRazas(dbHelper: DBHelper) {
        firestore.collection("razas").get().addOnSuccessListener { snapshot ->
            val razas = snapshot.toObjects(Raza::class.java)

            // Sincronización local
            for (raza in razas) {
                try {
                    dbHelper.insertarRaza(raza)
                } catch (e: Exception) {
                    dbHelper.actualizarRaza(raza)
                }
            }
            _listaRazas.value = razas
        }
    }

    fun guardarRaza(raza: Raza, dbHelper: DBHelper, onResult: (Boolean) -> Unit) {
        val db = firestore.collection("razas")
        val doc = if (raza.id.isEmpty()) db.document() else db.document(raza.id)
        val razaFinal = if (raza.id.isEmpty()) raza.copy(id = doc.id) else raza

        doc.set(razaFinal)
            .addOnSuccessListener {
                try {
                    if (raza.id.isEmpty()) dbHelper.insertarRaza(razaFinal)
                    else dbHelper.actualizarRaza(razaFinal)
                } catch (e: Exception) {
                    dbHelper.actualizarRaza(razaFinal)
                }

                cargarRazas(dbHelper)
                onResult(true)
            }
            .addOnFailureListener { onResult(false) }
    }

    fun eliminarRaza(id: String, dbHelper: DBHelper, onResult: (Boolean, String?) -> Unit) {
        // Verificación de dependencias en la nube antes de borrar
        firestore.collection("mascotas").whereEqualTo("id_raza", id).limit(1).get()
            .addOnSuccessListener { snapshotMascotas ->
                if (!snapshotMascotas.isEmpty) {
                    onResult(false, "No se puede eliminar: Esta raza está asignada a una mascota de la clínica.")
                    return@addOnSuccessListener
                }

                firestore.collection("mascota_adopcion").whereEqualTo("idRaza", id).limit(1).get()
                    .addOnSuccessListener { snapshotAdopcion ->
                        if (!snapshotAdopcion.isEmpty) {
                            onResult(false, "No se puede eliminar: Hay mascotas en adopción vinculadas.")
                        } else {
                            // Borrado en Firebase
                            firestore.collection("razas").document(id).delete()
                                .addOnSuccessListener {
                                    // Borrado en SQLite local
                                    dbHelper.eliminarRaza(id)
                                    cargarRazas(dbHelper)
                                    onResult(true, null)
                                }
                                .addOnFailureListener {
                                    onResult(false, "Error técnico al intentar eliminar la raza.")
                                }
                        }
                    }
            }
    }

    fun obtenerRazasPorEspecie(idEspecie: String, dbHelper: DBHelper): LiveData<List<Raza>> {
        val razasFiltradas = MutableLiveData<List<Raza>>()

        firestore.collection("razas")
            .whereEqualTo("idEspecie", idEspecie)
            .get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.toObjects(Raza::class.java)

                // Actualizar SQLite local con los resultados filtrados
                for (r in lista) {
                    try { dbHelper.insertarRaza(r) } catch (e: Exception) { dbHelper.actualizarRaza(r) }
                }

                razasFiltradas.value = lista
            }
            .addOnFailureListener {
                // Si no hay conexión, cargar desde el catálogo local de SQLite
                razasFiltradas.value = dbHelper.listarRazasPorEspecie(idEspecie)
            }

        return razasFiltradas
    }
}
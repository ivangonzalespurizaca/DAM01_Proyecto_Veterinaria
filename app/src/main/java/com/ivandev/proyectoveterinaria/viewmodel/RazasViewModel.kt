package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.model.Raza

class RazasViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _listaRazas = MutableLiveData<List<Raza>>()
    val listaRazas: LiveData<List<Raza>> get() = _listaRazas

    fun cargarRazas() {
        firestore.collection("razas").get().addOnSuccessListener { snapshot ->
            _listaRazas.value = snapshot.toObjects(Raza::class.java)
        }
    }

    fun guardarRaza(raza: Raza, onResult: (Boolean) -> Unit) {
        val db = firestore.collection("razas")
        val doc = if (raza.id.isEmpty()) db.document() else db.document(raza.id)
        val razaFinal = if (raza.id.isEmpty()) raza.copy(id = doc.id) else raza

        doc.set(razaFinal)
            .addOnSuccessListener { cargarRazas(); onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun eliminarRaza(id: String, onResult: (Boolean, String?) -> Unit) {
        // 1. Primera parada: Verificar en la colección de mascotas generales
        firestore.collection("mascotas").whereEqualTo("id_raza", id).limit(1).get()
            .addOnSuccessListener { snapshotMascotas ->
                if (!snapshotMascotas.isEmpty) {
                    onResult(false, "No se puede eliminar: Esta raza está asignada a una mascota de la clínica.")
                    return@addOnSuccessListener
                }

                firestore.collection("mascota_adopcion").whereEqualTo("idRaza", id).limit(1).get()
                    .addOnSuccessListener { snapshotAdopcion ->
                        if (!snapshotAdopcion.isEmpty) {
                            onResult(false, "No se puede eliminar: Hay mascotas en adopción.")
                        } else {
                            // 3. Si ambas están vacías, procedemos al borrado final
                            firestore.collection("razas").document(id).delete()
                                .addOnSuccessListener {
                                    cargarRazas()
                                    onResult(true, null)
                                }
                                .addOnFailureListener {
                                    onResult(false, "Error técnico al intentar eliminar la raza.")
                                }
                        }
                    }
                    .addOnFailureListener { onResult(false, "Error al conectar con el catálogo de adopción.") }
            }
            .addOnFailureListener { onResult(false, "Error al verificar la base de datos de la clínica.") }
    }

    fun obtenerRazasPorEspecie(idEspecie: String): LiveData<List<Raza>> {
        val razasFiltradas = MutableLiveData<List<Raza>>()

        firestore.collection("razas")
            .whereEqualTo("idEspecie", idEspecie) // Asegúrate que en Firestore el campo se llame igual que en tu modelo
            .get()
            .addOnSuccessListener { snapshot ->
                razasFiltradas.value = snapshot.toObjects(Raza::class.java)
            }
            .addOnFailureListener {
                razasFiltradas.value = emptyList()
            }

        return razasFiltradas
    }
}
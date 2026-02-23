package com.ivandev.proyectoveterinaria.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.model.Especie

class EspeciesViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _listaEspecies = MutableLiveData<List<Especie>>()
    val listaEspecies: LiveData<List<Especie>> get() = _listaEspecies

    fun cargarEspecies() {
        firestore.collection("especies")
            .get()
            .addOnSuccessListener { snapshot ->
                val especies = snapshot.toObjects(Especie::class.java)
                _listaEspecies.value = especies
            }
    }

    fun guardarEspecie(especie: Especie, onResult: (Boolean) -> Unit) {
        val db = firestore.collection("especies")

        if (especie.id.isEmpty()) {
            val nuevoDoc = db.document()
            val especieConId = especie.copy(id = nuevoDoc.id)

            nuevoDoc.set(especieConId)
                .addOnSuccessListener {
                    cargarEspecies()
                    onResult(true)
                }
                .addOnFailureListener { onResult(false) }
        } else {
            db.document(especie.id).set(especie)
                .addOnSuccessListener {
                    cargarEspecies()
                    onResult(true)
                }
                .addOnFailureListener { onResult(false) }
        }
    }


    fun eliminarEspecie(id: String, onResult: (Boolean, String?) -> Unit) {
        firestore.collection("razas")
            .whereEqualTo("idEspecie", id)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    firestore.collection("especies").document(id).delete()
                        .addOnSuccessListener {
                            cargarEspecies()
                            onResult(true, null)
                        }
                        .addOnFailureListener { e ->
                            onResult(false, "Error de red: ${e.message}")
                        }
                } else {
                    onResult(false, "No se puede eliminar: Existen razas asociadas.")
                }
            }
            .addOnFailureListener { e ->
                onResult(false, "Error al verificar dependencias: ${e.message}")
            }
    }
}
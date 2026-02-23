package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.model.MascotaAdopcion

class MascotaAdopcionViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _listaMascotas = MutableLiveData<List<MascotaAdopcion>>()

    val listaMascotas: LiveData<List<MascotaAdopcion>> get() = _listaMascotas

    fun cargarMascotas() {
        firestore.collection("mascota_adopcion")
            .get()
            .addOnSuccessListener { snapshot ->
                _listaMascotas.value = snapshot.toObjects(MascotaAdopcion::class.java)
            }
    }

    fun guardarMascota(mascota: MascotaAdopcion, onResult: (Boolean) -> Unit) {
        val documento = if (mascota.idMascotaAdopcion.isEmpty()) {
            val nuevoDoc = firestore.collection("mascota_adopcion").document()
            mascota.idMascotaAdopcion = nuevoDoc.id
            nuevoDoc
        } else {
            firestore.collection("mascota_adopcion").document(mascota.idMascotaAdopcion)
        }

        // 3. Guardamos los datos
        documento.set(mascota)
            .addOnSuccessListener {
                cargarMascotas() // Refrescamos la lista local
                onResult(true)
            }
            .addOnFailureListener { onResult(false) }
    }

    fun eliminarMascota(mascota: MascotaAdopcion, onResult: (Boolean, String?) -> Unit) {
        if (mascota.estado == "Disponible") {
            firestore.collection("mascota_adopcion")
                .document(mascota.idMascotaAdopcion)
                .delete()
                .addOnSuccessListener {
                    cargarMascotas()
                    onResult(true, null)
                }
                .addOnFailureListener { e ->
                    onResult(false, e.message)
                }
        } else {
            onResult(false, "No se puede eliminar: la mascota se encuentra en '${mascota.estado}'")
        }
    }
}
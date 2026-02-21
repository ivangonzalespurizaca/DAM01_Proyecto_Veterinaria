package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.model.Usuario
import com.ivandev.proyectoveterinaria.model.VeterinarioCompleto

class PersonalViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _listaPersonal = MutableLiveData<List<VeterinarioCompleto>>()
    val listaPersonal: LiveData<List<VeterinarioCompleto>> get() = _listaPersonal

    fun cargarVeterinarios() {
        firestore.collection("usuarios")
            .whereEqualTo("rol", "Veterinario")
            .get()
            .addOnSuccessListener { usersSnapshot ->
                val usuarios = usersSnapshot.toObjects(Usuario::class.java)

                firestore.collection("detalles_veterinarios")
                    .get()
                    .addOnSuccessListener { detailsSnapshot ->
                        val detallesMap = detailsSnapshot.documents.associateBy { it.id }

                        val listaCompleta = usuarios.map { user ->
                            val docDetalle = detallesMap[user.id]
                            VeterinarioCompleto(
                                usuario = user,
                                especialidad = docDetalle?.getString("especialidad"),
                                colegiatura = docDetalle?.getString("numero_colegiatura")
                            )
                        }
                        _listaPersonal.value = listaCompleta
                    }
            }
    }

    fun cambiarEstadoCuenta(uid: String, nuevoEstado: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("usuarios").document(uid)
            .update("estado", nuevoEstado)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
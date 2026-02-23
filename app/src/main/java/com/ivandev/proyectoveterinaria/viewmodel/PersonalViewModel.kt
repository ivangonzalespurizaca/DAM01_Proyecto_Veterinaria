package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.model.Usuario
import com.ivandev.proyectoveterinaria.model.Veterinario
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
                                sede = docDetalle?.getString("sede"),
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

    fun registrarNuevoVeterinario(
        context: android.content.Context,
        u: Usuario,
        v: Veterinario,
        pass: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val options = com.google.firebase.FirebaseApp.getInstance().options
        val secondaryApp = try {
            com.google.firebase.FirebaseApp.initializeApp(context, options, "temp_app")
        } catch (e: Exception) {
            com.google.firebase.FirebaseApp.getInstance("temp_app")
        }

        val secondaryAuth = com.google.firebase.auth.FirebaseAuth.getInstance(secondaryApp)

        secondaryAuth.createUserWithEmailAndPassword(u.correo, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                u.id = uid
                v.idUsuario = uid

                // 2. Guardar en la colección 'usuarios'
                firestore.collection("usuarios").document(uid).set(u)
                    .addOnSuccessListener {

                        val dataDetalles = mapOf(
                            "especialidad" to v.especialidad,
                            "numero_colegiatura" to v.numColegiatura,
                            "sede" to v.sede
                        )

                        firestore.collection("detalles_veterinarios").document(uid).set(dataDetalles)
                            .addOnSuccessListener {

                                secondaryAuth.sendPasswordResetEmail(u.correo)
                                secondaryApp.delete()
                                onResult(true, "Veterinario registrado correctamente")
                            }
                            .addOnFailureListener { onResult(false, "Error al guardar detalles profesionales") }
                    }
                    .addOnFailureListener { onResult(false, "Error al guardar datos de usuario") }
            }
            .addOnFailureListener { exception ->
                onResult(false, "Error en el registro: ${exception.message}")
            }
    }
}
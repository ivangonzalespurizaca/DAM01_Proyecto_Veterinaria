package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ivandev.proyectoveterinaria.model.Mascota

class MascotaViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var mascotaListener: ListenerRegistration? = null

    private val _listaMascotas = MutableLiveData<List<Mascota>>()
    val listaMascotas: LiveData<List<Mascota>> get() = _listaMascotas

    // 1. Función para guardar o actualizar
    fun guardarMascota(mascota: Mascota, onResult: (Boolean) -> Unit) {
        val documento = if (mascota.idMascota.isEmpty()) {
            val nuevoDoc = firestore.collection("mascotas").document()
            mascota.idMascota = nuevoDoc.id
            nuevoDoc
        } else {
            firestore.collection("mascotas").document(mascota.idMascota)
        }

        documento.set(mascota)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    // 2. Cargar con SnapshotListener (Tiempo Real)
    fun cargarMascotasPorCliente(idCliente: String) {
        // Removemos el listener anterior si existe para evitar duplicidad
        mascotaListener?.remove()

        mascotaListener = firestore.collection("mascotas")
            .whereEqualTo("idCliente", idCliente)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    _listaMascotas.value = snapshot.toObjects(Mascota::class.java)
                }
            }
    }

    fun obtenerDniUsuarioLogueado(uid: String, onResult: (String?) -> Unit) {
        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val dni = document.getString("dni")
                    onResult(dni)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { onResult(null) }
    }

    // 3. Buscar ID de usuario por DNI
    fun buscarClientePorDni(dni: String, onResult: (String?) -> Unit) {
        firestore.collection("usuarios")
            .whereEqualTo("dni", dni)
            .limit(1)
            .get()
            .addOnSuccessListener { query ->
                if (!query.isEmpty) {
                    onResult(query.documents[0].id)
                } else {
                    onResult(null)
                }
            }
    }

    // 4. Eliminación con validación de integridad médica
    fun eliminarMascota(mascota: Mascota, onResult: (Boolean, String?) -> Unit) {
        // Verificamos historial de consultas
        firestore.collection("consulta")
            .whereEqualTo("id_mascota", mascota.idMascota)
            .limit(1).get()
            .addOnSuccessListener { snapshotConsultas ->
                if (!snapshotConsultas.isEmpty) {
                    onResult(false, "No se puede eliminar: El paciente tiene un historial de consultas médicas.")
                    return@addOnSuccessListener
                }

                // Verificamos carnet de vacunas
                firestore.collection("vacuna_aplicada")
                    .whereEqualTo("id_mascota", mascota.idMascota)
                    .limit(1).get()
                    .addOnSuccessListener { snapshotVacunas ->
                        if (!snapshotVacunas.isEmpty) {
                            onResult(false, "No se puede eliminar: El paciente tiene vacunas aplicadas.")
                        } else {
                            // Eliminación final
                            firestore.collection("mascotas").document(mascota.idMascota).delete()
                                .addOnSuccessListener { onResult(true, null) }
                                .addOnFailureListener { onResult(false, "Error al intentar eliminar el registro.") }
                        }
                    }
            }
    }

    // Búsqueda por ID único (Para el QR)
    fun buscarPorIdUnico(id: String, onResult: (Mascota?) -> Unit) {
        firestore.collection("mascotas").document(id).get()
            .addOnSuccessListener { document ->
                onResult(document.toObject(Mascota::class.java))
            }
            .addOnFailureListener { onResult(null) }
    }

    // Búsqueda por DNI (Puede retornar varias mascotas)
    fun buscarPorDniDueno(dni: String, onResult: (List<Mascota>) -> Unit) {
        firestore.collection("mascotas")
            .whereEqualTo("dniDueno", dni)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.toObjects(Mascota::class.java))
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

// ... dentro de tu clase MascotaViewModel ...

    fun buscarMascotasPorFiltroGeneral(query: String, onResult: (List<Mascota>) -> Unit) {
        // 1. Limpiamos espacios y aseguramos que sea String
        val queryLimpia = query.trim()

        firestore.collection("mascotas")
            .where(
                Filter.or(
                    Filter.equalTo("idMascota", queryLimpia),
                    Filter.equalTo("dniDueno", queryLimpia) // Debe coincidir letra por letra con Firebase
                )
            )
            .get()
            .addOnSuccessListener { snapshot ->
                // 2. Mapeamos la lista
                val lista = snapshot.toObjects(Mascota::class.java)
                onResult(lista)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    override fun onCleared() {
        super.onCleared()
        mascotaListener?.remove() // Limpiamos el listener al destruir el ViewModel
    }
}
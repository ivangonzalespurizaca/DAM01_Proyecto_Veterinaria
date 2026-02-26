package com.ivandev.proyectoveterinaria.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ivandev.proyectoveterinaria.model.MascotaAdopcion
import com.ivandev.proyectoveterinaria.model.SolicitudAdopcion
import kotlinx.coroutines.launch
import com.ivandev.proyectoveterinaria.network.MascotaAdopcionModel
import com.ivandev.proyectoveterinaria.network.RetrofitClient

class MascotaAdopcionViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // Listas observables
    private val _listaMascotas = MutableLiveData<List<MascotaAdopcion>>()
    val listaMascotas: LiveData<List<MascotaAdopcion>> get() = _listaMascotas

    private val _solicitudes = MutableLiveData<List<SolicitudAdopcion>>()
    val solicitudes: LiveData<List<SolicitudAdopcion>> get() = _solicitudes

    // Variables de control para evitar "rebotes" y fugas de datos
    private var listaCompletaSolicitudes = listOf<SolicitudAdopcion>()
    private var filtroEstadoActual = "Todos"
    val mensajeEstado = MutableLiveData<String>()

    // --- SECCIÓN: VETERINARIO (Solicitudes Globales) ---

    fun guardarMascota(mascota: MascotaAdopcion, onResult: (Boolean) -> Unit) {
        val documento = if (mascota.idMascotaAdopcion.isEmpty()) {
            val nuevoDoc = firestore.collection("mascota_adopcion").document()
            mascota.idMascotaAdopcion = nuevoDoc.id
            nuevoDoc
        } else {
            firestore.collection("mascota_adopcion").document(mascota.idMascotaAdopcion)
        }

        documento.set(mascota)
            .addOnSuccessListener { onResult(true) } // El SnapshotListener refrescará la lista solo
            .addOnFailureListener { onResult(false) }
    }

    fun cargarSolicitudes() {
        firestore.collection("solicitudes_adopcion")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                listaCompletaSolicitudes = snapshot?.toObjects(SolicitudAdopcion::class.java) ?: emptyList()
                aplicarFiltroSolicitudes() // Re-aplica el filtro tras cada cambio
            }
    }

    fun filtrarPorEstado(estado: String) {
        filtroEstadoActual = estado
        aplicarFiltroSolicitudes()
    }

    private fun aplicarFiltroSolicitudes() {
        _solicitudes.value = if (filtroEstadoActual == "Todos") listaCompletaSolicitudes
        else listaCompletaSolicitudes.filter { it.estado == filtroEstadoActual }
    }

    // --- SECCIÓN: CLIENTE (Ver sus propias solicitudes) ---

    fun cargarSolicitudesParaCliente(userId: String) {
        firestore.collection("solicitudes_adopcion")
            .whereEqualTo("idCliente", userId)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val lista = snapshot?.toObjects(SolicitudAdopcion::class.java) ?: emptyList()


                listaCompletaSolicitudes = lista
                aplicarFiltroSolicitudes()
            }
    }

    // --- SECCIÓN: GESTIÓN ATÓMICA (Batch) ---

    fun actualizarEstado(solicitud: SolicitudAdopcion, nuevoEstado: String, mensaje: String = "", fecha: String = "", onResult: (Boolean) -> Unit) {
        val batch = firestore.batch()
        val refSol = firestore.collection("solicitudes_adopcion").document(solicitud.idSolicitud)

        val updates = mapOf(
            "estado" to nuevoEstado,
            "mensajeVet" to mensaje.ifEmpty { solicitud.mensajeVet },
            "fechaEntrevista" to fecha.ifEmpty { solicitud.fechaEntrevista }
        )
        batch.update(refSol, updates)

        if (nuevoEstado == "Aprobada") {
            val refMas = firestore.collection("mascota_adopcion").document(solicitud.idMascota)
            batch.update(refMas, "estado", "Adoptado")
        }

        batch.commit()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun eliminarMascota(mascota: MascotaAdopcion, onResult: (Boolean, String?) -> Unit) {
        // Regla de negocio: Solo se eliminan si están disponibles para evitar romper procesos activos
        if (mascota.estado == "Disponible") {
            firestore.collection("mascota_adopcion")
                .document(mascota.idMascotaAdopcion)
                .delete()
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { e -> onResult(false, e.message) }
        } else {
            onResult(false, "No se puede eliminar: la mascota se encuentra en '${mascota.estado}'")
        }
    }



    fun cargarMascotas() {
        // Iniciamos una corrutina para no bloquear el hilo principal (UI)
        viewModelScope.launch {
            mensajeEstado.postValue("Conectando con el servidor...")
            try {
                // 1. INTENTO POR API (Render/PostgreSQL)
                val response = RetrofitClient.mascotaAdopcionService.listarMascotas()

                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    // Mapeamos el modelo de la API al modelo que usa tu App si son diferentes
                    val listaMascotasApi = response.body()!!.map { apiModel ->
                        MascotaAdopcion(
                            idMascotaAdopcion = apiModel.idMascota ?: "",
                            nombreMascota = apiModel.nombreMascota,
                            idRaza = apiModel.idRaza,
                            idEspecie = apiModel.idEspecie,
                            nombreRaza = apiModel.nombreRaza,
                            nombreEspecie = apiModel.nombreEspecie,
                            sexo = apiModel.sexo,
                            descripcion = apiModel.descripcion,
                            contacto = apiModel.contacto,
                            edadEstimada = apiModel.edadEstimada,
                            estado = apiModel.estado,
                            foto = apiModel.fotoUrl
                        )
                    }
                    _listaMascotas.postValue(listaMascotasApi)
                    mensajeEstado.postValue("Datos actualizados desde Render")
                } else {
                    mensajeEstado.postValue("Servidor en reposo. Cargando respaldo...")
                    cargarMascotasDesdeFirebase()
                }
            } catch (e: Exception) {
                // 3. FALLBACK: Si hay error de red (Timeout en Render o sin internet)
                cargarMascotasDesdeFirebase()
            }
        }
    }

    private fun cargarMascotasDesdeFirebase() {
        firestore.collection("mascota_adopcion")
            .get()
            .addOnSuccessListener { snapshot ->
                _listaMascotas.value = snapshot.toObjects(MascotaAdopcion::class.java)
            }
    }

    // --- SECCIÓN: CATÁLOGO DE MASCOTAS ---

    fun cargarMascotasParaCliente() {
        firestore.collection("mascota_adopcion")
            .whereIn("estado", listOf("Disponible", "En Proceso")) // Solo lo no adoptado
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                _listaMascotas.value = snapshot?.toObjects(MascotaAdopcion::class.java) ?: emptyList()
            }
    }

    // FIX: Eliminamos el llamado a cargarMascotas() en el éxito de la actualización
    fun generarSolicitudAutomatica(mascota: MascotaAdopcion, onResult: (Boolean) -> Unit) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (userId.isEmpty()) return onResult(false)

        firestore.collection("usuarios").document(userId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                val nuevaSolicitud = SolicitudAdopcion(
                    idSolicitud = firestore.collection("solicitudes_adopcion").document().id,
                    idMascota = mascota.idMascotaAdopcion,
                    nombreMascota = mascota.nombreMascota,
                    fotoMascota = mascota.foto,
                    idCliente = userId,
                    nombreCliente = document.getString("nombreCompleto") ?: "",
                    telefonoCliente = document.getString("celular") ?: "",
                    estado = "Pendiente"
                )

                firestore.collection("solicitudes_adopcion").document(nuevaSolicitud.idSolicitud).set(nuevaSolicitud)
                    .addOnSuccessListener {
                        actualizarEstadoMascota(mascota.idMascotaAdopcion, "En Proceso")
                        onResult(true)
                    }
            }
        }
    }

    private fun actualizarEstadoMascota(idMascota: String, nuevoEstado: String) {
        firestore.collection("mascota_adopcion").document(idMascota)
            .update("estado", nuevoEstado)
        // IMPORTANTE: Ya no llamamos a cargarMascotas(). El Listener de tiempo real se encarga.
    }

    // Dentro de MascotaAdopcionViewModel
    fun verificarSiYaPostulo(idMascota: String, callback: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Suponiendo que tu colección de solicitudes se llama "solicitudes_adopcion"
        firestore.collection("solicitudes_adopcion")
            .whereEqualTo("idMascota", idMascota)
            .whereEqualTo("idCliente", userId)
            .get()
            .addOnSuccessListener { documentos ->
                // Si el resultado no está vacío, es que ya existe una solicitud
                callback(!documentos.isEmpty)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun filtrarMascotas(query: String): List<MascotaAdopcion> {
        val listaActual = _listaMascotas.value ?: emptyList()
        return if (query.isEmpty()) listaActual
        else listaActual.filter { it.nombreMascota.contains(query, true) || it.nombreRaza.contains(query, true) }
    }
}
package com.ivandev.proyectoveterinaria.fragment.admin.catalogo

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import androidx.core.os.BundleCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentRegistrarMascotaAdopcionBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.MascotaAdopcion
import com.ivandev.proyectoveterinaria.viewmodel.EspeciesViewModel
import com.ivandev.proyectoveterinaria.viewmodel.MascotaAdopcionViewModel
import com.ivandev.proyectoveterinaria.viewmodel.RazasViewModel

class RegistrarMascotaAdopcionFragment : Fragment(R.layout.fragment_registrar_mascota_adopcion), IFragmentoToolbar {
    override val titulo: String
        get() = if (mascotaEditar != null) "EDITAR MASCOTA" else "NUEVA MASCOTA"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO
    private var _binding: FragmentRegistrarMascotaAdopcionBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private var especieSeleccionadaId: String = ""
    private var razaSeleccionadaId: String = ""
    private val galeriaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            Glide.with(requireContext()).load(it).circleCrop().into(binding.ivFotoMascota)
        }
    }
    private val viewModel: MascotaAdopcionViewModel by viewModels()
    private val especieViewModel: EspeciesViewModel by viewModels()
    private val razaViewModel: RazasViewModel by viewModels()
    private var mascotaEditar: MascotaAdopcion? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrarMascotaAdopcionBinding.bind(view)

        mascotaEditar = arguments?.let {
            BundleCompat.getParcelable(it, "mascota", MascotaAdopcion::class.java)
        }

        actualizarTituloToolbar()

        setupCatalogos()

        configurarModo()

        binding.btnEliminarMascota.setOnClickListener {
            confirmarEliminacion()
        }
        binding.btnCambiarFoto.setOnClickListener {
            binding.btnCambiarFoto.setOnClickListener { galeriaLauncher.launch("image/*") }
        }
        binding.btnGuardarMascota.setOnClickListener {
            validarYGuardar()
        }
    }

    private fun configurarModo() {
        val esEdicion = mascotaEditar != null

        // Unificamos textos y visibilidad
        binding.tvTituloMascota.text = if (esEdicion) "Editar información de la mascota" else "Registrar nueva mascota para adopción"
        binding.btnGuardarMascota.text = if (esEdicion) "ACTUALIZAR" else "REGISTRAR"
        binding.btnEliminarMascota.visibility = if (esEdicion) View.VISIBLE else View.GONE

        if (esEdicion) {
            rellenarCampos(mascotaEditar!!)
        }
    }

    private fun rellenarCampos(mascota: MascotaAdopcion) {
        binding.apply {
            etNombreMascota.setText(mascota.nombreMascota)
            etEdadEstimada.setText(mascota.edadEstimada)
            actvSexo.setText(mascota.sexo, false)

            // Cargamos la foto actual con Glide
            Glide.with(requireContext())
                .load(mascota.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoMascota)
        }
    }

    private fun confirmarEliminacion() {
        mascotaEditar?.let { mascota ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("¿Eliminar mascota?")
            builder.setMessage("¿Estás seguro de que deseas eliminar a ${mascota.nombreMascota}? Esta acción no se puede deshacer.")

            builder.setPositiveButton("ELIMINAR") { _, _ ->
                viewModel.eliminarMascota(mascota) { exito, mensaje ->
                    if (exito) {
                        Toast.makeText(context, "Mascota eliminada con éxito", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            }

            builder.setNegativeButton("CANCELAR", null)

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun validarYGuardar() {
        val nombre = binding.etNombreMascota.text.toString().trim()
        val edad = binding.etEdadEstimada.text.toString().trim()
        val sexo = binding.actvSexo.text.toString()

        // 1. Validaciones básicas
        if (nombre.isEmpty() || especieSeleccionadaId.isEmpty() || razaSeleccionadaId.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, completa los datos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Decidir el flujo de la foto
        if (imageUri != null) {
            // Si hay una nueva imagen seleccionada, la subimos primero
            subirACloudinary(nombre, edad, sexo)
        } else if (mascotaEditar != null) {
            // Si es edición y no se cambió la foto, usamos la URL que ya existía
            guardarEnFirestore(mascotaEditar!!.foto, nombre, edad, sexo)
        } else {
            // Si es registro nuevo y no hay foto, avisamos al usuario
            Toast.makeText(requireContext(), "Es necesario incluir una foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun subirACloudinary(nombre: String, edad: String, sexo: String) {
        // Bloqueamos el botón para evitar múltiples clics
        binding.btnGuardarMascota.isEnabled = false
        Toast.makeText(requireContext(), "Subiendo imagen...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(imageUri)
            .unsigned("vet_project")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val urlSegura = resultData?.get("secure_url").toString()
                    guardarEnFirestore(urlSegura, nombre, edad, sexo)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    binding.btnGuardarMascota.isEnabled = true
                    Toast.makeText(requireContext(), "Error al subir foto", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun guardarEnFirestore(urlFoto: String, nombre: String, edad: String, sexo: String) {
        val mascota = MascotaAdopcion(
            idMascotaAdopcion = mascotaEditar?.idMascotaAdopcion ?: "",
            idRaza = razaSeleccionadaId,
            idEspecie = especieSeleccionadaId,
            nombreMascota = nombre,
            sexo = sexo,
            edadEstimada = edad,
            estado = mascotaEditar?.estado ?: "Disponible",
            foto = urlFoto
        )

        // 4. Llamamos al ViewModel para la persistencia
        viewModel.guardarMascota(mascota) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "Mascota guardada exitosamente", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack() // Volvemos al listado
            } else {
                binding.btnGuardarMascota.isEnabled = true
                Toast.makeText(requireContext(), "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCatalogos() {
        especieViewModel.cargarEspecies()
        especieViewModel.listaEspecies.observe(viewLifecycleOwner) { especies ->
            val nombres = especies.map { it.nombre }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, nombres)
            binding.actvEspecie.setAdapter(adapter)

            val opcionesSexo = arrayOf("Macho", "Hembra")
            val adapterSexo = ArrayAdapter(requireContext(), R.layout.list_item, opcionesSexo)
            binding.actvSexo.setAdapter(adapterSexo)

            // Si estamos editando, buscamos el nombre por el ID
            if (mascotaEditar != null) {
                especieSeleccionadaId = mascotaEditar!!.idEspecie
                val nombreEspecie = especies.find { it.id == especieSeleccionadaId }?.nombre
                binding.actvEspecie.setText(nombreEspecie, false)
                cargarRazasPorEspecie(especieSeleccionadaId) // Cargar razas de esa especie
            }
        }

        // 2. Listener al elegir una especie
        binding.actvEspecie.setOnItemClickListener { _, _, position, _ ->
            especieViewModel.listaEspecies.value?.get(position)?.let { especie ->
                especieSeleccionadaId = especie.id
                razaSeleccionadaId=""
                binding.actvRaza.setText("", false) // Limpiar raza anterior
                cargarRazasPorEspecie(especie.id)
            }
        }
    }

    private fun cargarRazasPorEspecie(idEspecie: String) {
        // Filtramos las razas que pertenecen a la especie seleccionada
        razaViewModel.obtenerRazasPorEspecie(idEspecie).observe(viewLifecycleOwner) { razas ->
            if (razas.isEmpty()){
                binding.tilRazaMascota.error = "Sin razas registradas"
                val adapter = ArrayAdapter(requireContext(), R.layout.list_item, arrayOf<String>())
                binding.actvRaza.setAdapter(adapter)
            }else{
                binding.tilRazaMascota.error = null
                val nombres = razas.map { it.nombre }
                val adapter = ArrayAdapter(requireContext(), R.layout.list_item, nombres)
                binding.actvRaza.setAdapter(adapter)

                if (mascotaEditar != null && razaSeleccionadaId.isEmpty()) {
                    razaSeleccionadaId = mascotaEditar!!.idRaza
                    val nombreRaza = razas.find { it.id == razaSeleccionadaId }?.nombre
                    binding.actvRaza.setText(nombreRaza, false)
                }
            }

            binding.actvRaza.setOnItemClickListener { _, _, position, _ ->
                razaSeleccionadaId = razas[position].id
            }
        }
    }

    private fun actualizarTituloToolbar() {
        (activity as? PanelPrincipalActivity)?.let {
            it.supportActionBar?.title = this.titulo
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
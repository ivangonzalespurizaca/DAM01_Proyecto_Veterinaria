package com.ivandev.proyectoveterinaria.fragment.admin.catalogo

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
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
import java.util.UUID

class RegistrarMascotaAdopcionFragment : Fragment(R.layout.fragment_registrar_mascota_adopcion), IFragmentoToolbar {
    override val titulo: String get() = if (mascotaEditar != null) "EDITAR MASCOTA" else "NUEVA MASCOTA"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO

    private var _binding: FragmentRegistrarMascotaAdopcionBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private var especieSeleccionadaId: String = ""
    private var razaSeleccionadaId: String = ""

    private val viewModel: MascotaAdopcionViewModel by viewModels()
    private val especieViewModel: EspeciesViewModel by viewModels()
    private val razaViewModel: RazasViewModel by viewModels()
    private var mascotaEditar: MascotaAdopcion? = null

    private val galeriaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            Glide.with(requireContext()).load(it).circleCrop().into(binding.ivFotoMascota)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrarMascotaAdopcionBinding.bind(view)

        mascotaEditar = arguments?.let {
            BundleCompat.getParcelable(it, "mascota", MascotaAdopcion::class.java)
        }

        actualizarTituloToolbar()
        setupCatalogos()
        configurarModo()

        binding.btnEliminarMascota.setOnClickListener { confirmarEliminacion() }
        binding.btnCambiarFoto.setOnClickListener { galeriaLauncher.launch("image/*") }
        binding.btnGuardarMascota.setOnClickListener { validarYGuardar() }
    }

    private fun configurarModo() {
        val esEdicion = mascotaEditar != null
        binding.tvTituloMascota.text = if (esEdicion) "Editar información de la mascota" else "Registrar nueva mascota para adopción"
        binding.btnGuardarMascota.text = if (esEdicion) "ACTUALIZAR" else "REGISTRAR"
        binding.btnEliminarMascota.visibility = if (esEdicion) View.VISIBLE else View.GONE

        if (esEdicion) rellenarCampos(mascotaEditar!!)
    }

    private fun rellenarCampos(m: MascotaAdopcion) {
        binding.apply {
            etNombreMascota.setText(m.nombreMascota)
            etEdadEstimada.setText(m.edadEstimada)
            etDescripcionAdopcion.setText(m.descripcion)
            etContactoAdopcion.setText(m.contacto)
            actvSexo.setText(m.sexo, false)

            Glide.with(requireContext())
                .load(m.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoMascota)
        }
    }

    private fun validarYGuardar() {
        val nombre = binding.etNombreMascota.text.toString().trim()
        val edad = binding.etEdadEstimada.text.toString().trim()
        val sexo = binding.actvSexo.text.toString()
        val desc = binding.etDescripcionAdopcion.text.toString().trim()
        val contacto = binding.etContactoAdopcion.text.toString().trim()

        // Captura de nombres para Desnormalización
        val nombreEsp = binding.actvEspecie.text.toString()
        val nombreRaz = binding.actvRaza.text.toString()

        if (nombre.isEmpty() || especieSeleccionadaId.isEmpty() || contacto.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre, especie y contacto son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            subirACloudinary(nombre, edad, sexo, desc, contacto, nombreEsp, nombreRaz)
        } else if (mascotaEditar != null) {
            guardarEnFirestore(mascotaEditar!!.foto, nombre, edad, sexo, desc, contacto, nombreEsp, nombreRaz)
        } else {
            Toast.makeText(requireContext(), "Es necesario incluir una foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun subirACloudinary(nombre: String, edad: String, sexo: String, desc: String, contacto: String, espNombre: String, razNombre: String) {
        binding.btnGuardarMascota.isEnabled = false
        Toast.makeText(requireContext(), "Subiendo imagen...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(imageUri)
            .unsigned("vet_project")
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val urlSegura = resultData?.get("secure_url").toString()
                    guardarEnFirestore(urlSegura, nombre, edad, sexo, desc, contacto, espNombre, razNombre)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    binding.btnGuardarMascota.isEnabled = true
                    Toast.makeText(requireContext(), "Error al subir foto", Toast.LENGTH_SHORT).show()
                }
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun guardarEnFirestore(url: String, nombre: String, edad: String, sexo: String, desc: String, contacto: String, espNombre: String, razNombre: String) {
        val mascota = MascotaAdopcion(
            idMascotaAdopcion = mascotaEditar?.idMascotaAdopcion ?: UUID.randomUUID().toString(),
            idRaza = razaSeleccionadaId,
            nombreRaza = razNombre,       // Desnormalizado
            idEspecie = especieSeleccionadaId,
            nombreEspecie = espNombre,    // Desnormalizado
            nombreMascota = nombre,
            sexo = sexo,
            edadEstimada = edad,
            descripcion = desc,
            contacto = contacto,
            // Estado automático: "Disponible" por defecto
            estado = mascotaEditar?.estado ?: "Disponible",
            foto = url
        )

        viewModel.guardarMascota(mascota) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "Guardado exitosamente", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                binding.btnGuardarMascota.isEnabled = true
                Toast.makeText(requireContext(), "Error al guardar en la nube", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCatalogos() {
        especieViewModel.cargarEspecies()
        especieViewModel.listaEspecies.observe(viewLifecycleOwner) { especies ->
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, especies.map { it.nombre })
            binding.actvEspecie.setAdapter(adapter)

            val adapterSexo = ArrayAdapter(requireContext(), R.layout.list_item, arrayOf("Macho", "Hembra"))
            binding.actvSexo.setAdapter(adapterSexo)

            if (mascotaEditar != null) {
                especieSeleccionadaId = mascotaEditar!!.idEspecie
                val nombreEspecie = especies.find { it.id == especieSeleccionadaId }?.nombre
                binding.actvEspecie.setText(nombreEspecie, false)
                cargarRazasPorEspecie(especieSeleccionadaId)
            }
        }

        binding.actvEspecie.setOnItemClickListener { _, _, position, _ ->
            especieViewModel.listaEspecies.value?.get(position)?.let {
                especieSeleccionadaId = it.id
                razaSeleccionadaId = ""
                binding.actvRaza.setText("", false)
                cargarRazasPorEspecie(it.id)
            }
        }
    }

    private fun cargarRazasPorEspecie(idEspecie: String) {
        razaViewModel.obtenerRazasPorEspecie(idEspecie).observe(viewLifecycleOwner) { razas ->
            if (razas.isEmpty()) {
                binding.tilRazaMascota.error = "Sin razas registradas"
                binding.actvRaza.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, arrayOf<String>()))
            } else {
                binding.tilRazaMascota.error = null
                binding.actvRaza.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, razas.map { it.nombre }))

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

    private fun confirmarEliminacion() {
        mascotaEditar?.let { mascota ->
            AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar mascota?")
                .setMessage("¿Estás seguro de que deseas eliminar a ${mascota.nombreMascota}?")
                .setPositiveButton("ELIMINAR") { _, _ ->
                    viewModel.eliminarMascota(mascota) { exito, mensaje ->
                        if (exito) {
                            Toast.makeText(context, "Mascota eliminada", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("CANCELAR", null).show()
        }
    }

    private fun actualizarTituloToolbar() {
        (activity as? PanelPrincipalActivity)?.let { it.supportActionBar?.title = this.titulo }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
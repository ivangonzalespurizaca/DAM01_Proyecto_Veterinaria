package com.ivandev.proyectoveterinaria.fragment.cliente.mascota

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
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
import com.google.firebase.auth.FirebaseAuth
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentRegistrarMascotaBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Mascota
import com.ivandev.proyectoveterinaria.viewmodel.EspeciesViewModel
import com.ivandev.proyectoveterinaria.viewmodel.MascotaViewModel
import com.ivandev.proyectoveterinaria.viewmodel.RazasViewModel
import java.util.*

class RegistrarMascotaFragment : Fragment(R.layout.fragment_registrar_mascota), IFragmentoToolbar {
    override val titulo: String get() = if (mascotaEditar != null) "EDITAR MASCOTA" else "NUEVA MASCOTA"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO

    private var nombreDueno: String = ""
    private var _binding: FragmentRegistrarMascotaBinding? = null
    private val binding get() = _binding!!
    val user = FirebaseAuth.getInstance().currentUser
    private var imageUri: Uri? = null
    private var especieSeleccionadaId: String = ""
    private var razaSeleccionadaId: String = ""

    private val viewModel: MascotaViewModel by viewModels()
    private val especieViewModel: EspeciesViewModel by viewModels()
    private val razaViewModel: RazasViewModel by viewModels()

    private var mascotaEditar: Mascota? = null

    private val galeriaLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            Glide.with(requireContext()).load(it).circleCrop().into(binding.ivFotoMascota)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrarMascotaBinding.bind(view)

        mascotaEditar = arguments?.let {
            BundleCompat.getParcelable(it, "mascota", Mascota::class.java)
        }

        actualizarTituloToolbar()
        setupCatalogos()
        setupDatePicker()
        configurarModo()

        binding.btnCambiarFoto.setOnClickListener { galeriaLauncher.launch("image/*") }
        binding.btnEliminarMascota.setOnClickListener { confirmarEliminacion() }
        binding.btnGuardarMascota.setOnClickListener { validarYGuardar() }
    }

    private fun configurarModo() {
        val esEdicion = mascotaEditar != null
        binding.btnGuardarMascota.text = if (esEdicion) "ACTUALIZAR" else "REGISTRAR"
        binding.btnEliminarMascota.visibility = if (esEdicion) View.VISIBLE else View.GONE

        if (esEdicion) {
            nombreDueno = mascotaEditar!!.nombreDueno
            rellenarCampos(mascotaEditar!!)
        } else {

            val prefs = requireActivity().getSharedPreferences("Sesion", Context.MODE_PRIVATE)
            nombreDueno = prefs.getString("nombreCompleto", "") ?: ""

            user?.uid?.let { uid ->
                viewModel.obtenerDniUsuarioLogueado(uid) { dni ->
                    if (dni != null) {
                        binding.etDniDueno.setText(dni)
                         binding.etDniDueno.isEnabled = false
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No se pudo cargar tu DNI",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun rellenarCampos(mascota: Mascota) {
        binding.apply {
            etDniDueno.setText(mascota.dniDueno)
            etNombreMascota.setText(mascota.nombreMascota)
            etPesoInicial.setText(mascota.pesoInicial.toString())
            etFechaNacimiento.setText(mascota.fechaNacimiento)
            actvSexo.setText(mascota.sexo, false)

            Glide.with(requireContext())
                .load(mascota.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoMascota)
        }
    }

    private fun setupDatePicker() {
        binding.etFechaNacimiento.setOnClickListener {
            val c = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->
                val mes = String.format("%02d", m + 1)
                val dia = String.format("%02d", d)
                val fechaISO = "$y-$mes-$dia" // Ejemplo: 2026-02-23
                binding.etFechaNacimiento.setText(fechaISO)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))

            datePicker.show()
        }
    }
    private fun validarYGuardar() {
        val nombre = binding.etNombreMascota.text.toString().trim()
        val peso = binding.etPesoInicial.text.toString().toDoubleOrNull() ?: 0.0
        val fecha = binding.etFechaNacimiento.text.toString()
        val sexo = binding.actvSexo.text.toString()
        val dni = binding.etDniDueno.text.toString()
        val especieNombre = binding.actvEspecie.text.toString()
        val nombreRaza = binding.actvRaza.text.toString()

        if (nombre.isEmpty() || especieSeleccionadaId.isEmpty() || razaSeleccionadaId.isEmpty()) {
            Toast.makeText(requireContext(), "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            subirACloudinary(nombre, peso, fecha, sexo, dni, especieNombre, nombreRaza)
        } else if (mascotaEditar != null) {
            guardarEnFirestore(mascotaEditar!!.foto, nombre, peso, fecha, sexo, dni, especieNombre, nombreRaza)
        } else {
            Toast.makeText(requireContext(), "Es necesaria una foto del paciente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun subirACloudinary(nombre: String, peso: Double, fecha: String, sexo: String, dni: String, especieNombre: String, razaNombre: String) {
        binding.btnGuardarMascota.isEnabled = false
        Toast.makeText(requireContext(), "Subiendo imagen clínica...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(imageUri)
            .unsigned("vet_project")
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url").toString()
                    guardarEnFirestore(url, nombre, peso, fecha, sexo, dni, especieNombre, razaNombre)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    binding.btnGuardarMascota.isEnabled = true
                    Toast.makeText(requireContext(), "Error en imagen", Toast.LENGTH_SHORT).show()
                }
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, b: Long, t: Long) {}
                override fun onReschedule(requestId: String?, e: ErrorInfo?) {}
            }).dispatch()
    }

    private fun guardarEnFirestore(url: String, nombre: String, peso: Double, fecha: String, sexo: String, dni: String, nombreEspecie: String, razaNombre: String) {
        val uidActual = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val mascota = Mascota(
            idMascota = mascotaEditar?.idMascota ?: "",
            nombreMascota = nombre,
            idEspecie = especieSeleccionadaId,
            nombreDueno = nombreDueno,
            nombreEspecie = nombreEspecie,
            nombreRaza = razaNombre,
            idRaza = razaSeleccionadaId,
            idCliente = mascotaEditar?.idCliente ?: uidActual,
            sexo = sexo,
            fechaNacimiento = fecha,
            pesoInicial = peso,
            dniDueno = dni,
            foto = url
        )

        viewModel.guardarMascota(mascota) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "Paciente registrado", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                binding.btnGuardarMascota.isEnabled = true
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
                val nombre = especies.find { it.id == especieSeleccionadaId }?.nombre
                binding.actvEspecie.setText(nombre, false)
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
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, razas.map { it.nombre })
            binding.actvRaza.setAdapter(adapter)

            if (mascotaEditar != null && razaSeleccionadaId.isEmpty()) {
                razaSeleccionadaId = mascotaEditar!!.idRaza
                val nombre = razas.find { it.id == razaSeleccionadaId }?.nombre
                binding.actvRaza.setText(nombre, false)
            }

            binding.actvRaza.setOnItemClickListener { _, _, i, _ ->
                razaSeleccionadaId = razas[i].id
            }
        }
    }

    private fun confirmarEliminacion() {
        mascotaEditar?.let { m ->
            AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar paciente?")
                .setMessage("Se verificará si ${m.nombreMascota} tiene historial médico antes de proceder.")
                .setPositiveButton("ELIMINAR") { _, _ ->
                    viewModel.eliminarMascota(m) { exito, msj ->
                        if (exito) {
                            Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(context, msj, Toast.LENGTH_LONG).show()
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
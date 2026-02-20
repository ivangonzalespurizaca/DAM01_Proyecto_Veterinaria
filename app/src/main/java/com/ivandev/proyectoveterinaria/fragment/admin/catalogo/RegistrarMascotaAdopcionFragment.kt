package com.ivandev.proyectoveterinaria.fragment.admin.catalogo

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentRegistrarMascotaAdopcionBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

class RegistrarMascotaAdopcionFragment : Fragment(R.layout.fragment_registrar_mascota_adopcion), IFragmentoToolbar {
    override val titulo: String = "NUEVA MASCOTA"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO
    private var _binding: FragmentRegistrarMascotaAdopcionBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null


    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageUri = it
            binding.ivFotoMascota.setImageURI(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrarMascotaAdopcionBinding.bind(view)

        configurarSpinner()

        binding.btnSeleccionarFoto.setOnClickListener { galleryLauncher.launch("image/*") }

        binding.btnGuardarMascota.setOnClickListener {
            if (validarCampos()) {
                subirImagenYGuardar()
            }
        }
    }

    private fun configurarSpinner() {
        val opciones = arrayOf("Macho", "Hembra")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, opciones)
        binding.spSexo.adapter = adapter
    }

    private fun validarCampos(): Boolean {
        if (binding.etNombreMascota.text.isNullOrBlank() || imageUri == null) {
            Toast.makeText(context, "Nombre y foto son obligatorios", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun subirImagenYGuardar() {
        binding.btnGuardarMascota.isEnabled = false
        Toast.makeText(context, "Subiendo imagen...", Toast.LENGTH_SHORT).show()

        // Usamos tu preset 'vet_proyect'
        MediaManager.get().upload(imageUri)
            .unsigned("vet_project")
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<out Any?, Any?>?) {
                    val urlFoto = resultData?.get("secure_url").toString()
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    binding.btnGuardarMascota.isEnabled = true
                    Toast.makeText(context, "Error en Cloudinary: ${error?.description}", Toast.LENGTH_SHORT).show()
                }
                // ... otros métodos del callback
                override fun onStart(requestId: String?) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            }).dispatch()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
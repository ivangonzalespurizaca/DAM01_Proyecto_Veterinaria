package com.ivandev.proyectoveterinaria.fragment.perfil

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentEditarDatosBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Usuario

class EditarDatosFragment : Fragment(R.layout.fragment_editar_datos), IFragmentoToolbar {
    // Configuración de la Toolbar
    override val titulo: String = "EDITAR DATOS"
    override val tipo = PanelPrincipalActivity.TipoToolbar.PERFIL

    private var _binding: FragmentEditarDatosBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var userRole: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditarDatosBinding.bind(view)

        cargarDatosActuales()

        configurarSelectorEspecialidades()
        configurarSelectorSedes()

        binding.btnGuardarCambios.setOnClickListener {
            guardarCambios()
        }
    }

    private fun cargarDatosActuales() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val usuario = doc.toObject(Usuario::class.java)
                usuario?.let {
                    binding.etNombre.setText(it.nombreCompleto)
                    binding.etDni.setText(it.dni)
                    binding.etCelular.setText(it.celular)
                    binding.etCorreo.setText(it.correo)
                    userRole = it.rol

                    if (it.rol == "Veterinario") {
                        binding.layoutCamposVeterinario.visibility = View.VISIBLE
                        cargarDetalleVeterinario(uid)
                    }
                }
            }
    }

    private fun cargarDetalleVeterinario(uid: String) {
        firestore.collection("detalles_veterinarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.actvEspecialidad.setText(doc.getString("especialidad"), false)
                    binding.etColegiatura.setText(doc.getString("numero_colegiatura"))
                    binding.actvSede.setText(doc.getString("sede"))
                }
            }
    }

    private fun guardarCambios() {
        val uid = auth.currentUser?.uid ?: return

        // Datos básicos (Solo los editables)
        val updatesUser = mapOf(
            "nombreCompleto" to binding.etNombre.text.toString(),
            "celular" to binding.etCelular.text.toString()
        )

        firestore.collection("usuarios").document(uid).update(updatesUser)
            .addOnSuccessListener {
                if (userRole == "Veterinario") {
                    guardarDetalleVeterinario(uid)
                } else {
                    finalizarEdicion()
                }
            }
    }

    private fun guardarDetalleVeterinario(uid: String) {
        val updatesVet = mapOf(
            "especialidad" to binding.actvEspecialidad.text.toString(),
            "numero_colegiatura" to binding.etColegiatura.text.toString(),
            "sede" to binding.actvSede.text.toString()
        )

        firestore.collection("detalles_veterinarios").document(uid)
            .set(updatesVet, SetOptions.merge())
            .addOnSuccessListener { finalizarEdicion() }
    }

    private fun finalizarEdicion() {
        Toast.makeText(requireContext(), "Datos actualizados", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun configurarSelectorEspecialidades() {
        val especialidades = resources.getStringArray(R.array.especialidades_veterinario)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            especialidades
        )

        binding.actvEspecialidad.setAdapter(adapter)
    }

    private fun configurarSelectorSedes() {
        val especialidades = resources.getStringArray(R.array.sedes_veterinario)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            especialidades
        )

        binding.actvSede.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
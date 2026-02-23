package com.ivandev.proyectoveterinaria.fragment.admin.usuario

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentRegistrarVeterinarioBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Usuario
import com.ivandev.proyectoveterinaria.model.Veterinario
import com.ivandev.proyectoveterinaria.viewmodel.PersonalViewModel

class RegistrarVeterinarioFragment : Fragment(R.layout.fragment_registrar_veterinario),
    IFragmentoToolbar {

    override val titulo: String = "REGISTRAR VETERINARIO"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO

    private var _binding: FragmentRegistrarVeterinarioBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonalViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrarVeterinarioBinding.bind(view)

        binding.btnRegistrarVet.setOnClickListener {
            validarYRegistrar()
        }
    }

    private fun validarYRegistrar() {
        val nombre = binding.etNombreVet.text.toString().trim()
        val dni = binding.etDniVet.text.toString().trim()
        val celular = binding.etCelularVet.text.toString().trim()
        val cmvp = binding.etCmvpVet.text.toString().trim()
        val correo = binding.etEmailVet.text.toString().trim()

        if (nombre.isEmpty() || dni.isEmpty() || celular.isEmpty() || cmvp.isEmpty() || correo.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (dni.length != 8) {
            Toast.makeText(requireContext(), "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevoUsuario = Usuario(
            nombreCompleto = nombre,
            dni = dni,
            celular = celular,
            correo = correo,
            rol = "Veterinario",
            estado = "Activo"
        )

        val detallesVet = Veterinario(
            numColegiatura = cmvp,
            especialidad = "Medicina General",
            sede = "Sede Principal"
        )

        viewModel.registrarNuevoVeterinario(requireContext(), nuevoUsuario, detallesVet, dni) { exito, mensaje ->
            if (exito) {
                Toast.makeText(requireContext(), "Veterinario registrado con éxito.", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Error: $mensaje", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.ivandev.proyectoveterinaria.fragment.registro

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.FragmentPaso1Binding
import com.ivandev.proyectoveterinaria.viewmodel.RegistroClienteViewModel

class Paso1Fragment : Fragment(R.layout.fragment_paso1) {
    private val registroClienteViewModel: RegistroClienteViewModel by activityViewModels()

    private var _binding: FragmentPaso1Binding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPaso1Binding.bind(view)

        binding.btnSiguientePaso1.setOnClickListener {
            guardarDatosYContinuar()
        }

        binding.txtLoginLink.setOnClickListener {
            activity?.finish()
        }
    }

    private fun guardarDatosYContinuar() {
        val nombre = binding.etNombre.text.toString().trim()
        val dni = binding.etDni.text.toString().trim()
        val celular = binding.etCelular.text.toString().trim()

        if (nombre.isEmpty() || dni.isEmpty() || celular.isEmpty()) {
            binding.etNombre.error = "Completa todos los campos"
            return
        }

        registroClienteViewModel.usuarioProceso.nombreCompleto = nombre
        registroClienteViewModel.usuarioProceso.dni = dni
        registroClienteViewModel.usuarioProceso.celular = celular

        findNavController().navigate(R.id.action_paso1_to_paso2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
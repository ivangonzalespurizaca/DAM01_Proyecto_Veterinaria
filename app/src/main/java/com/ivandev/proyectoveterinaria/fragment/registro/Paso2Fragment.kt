package com.ivandev.proyectoveterinaria.fragment.registro

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.UsuarioDBHelper
import com.ivandev.proyectoveterinaria.databinding.FragmentPaso2Binding
import com.ivandev.proyectoveterinaria.viewmodel.RegistroClienteViewModel

class Paso2Fragment : Fragment(R.layout.fragment_paso2) {
    private val registroClienteViewModel: RegistroClienteViewModel by activityViewModels()
    private var _binding: FragmentPaso2Binding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: UsuarioDBHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPaso2Binding.bind(view)
        dbHelper = UsuarioDBHelper(requireContext())
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.txtLoginLink.setOnClickListener {
            activity?.finish()
        }

        binding.btnFinalizarRegistro.setOnClickListener {
            val correo = binding.etEmail.text.toString().trim()
            val pass = binding.etContrasenia.text.toString().trim()
            val repeatPass = binding.etRepetirContrasenia.text.toString().trim()

            if (correo.isEmpty() || pass.isEmpty() || repeatPass != pass) {
                Toast.makeText(context, "Las contraseñas no coinciden o están vacías", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(correo, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { verifyTask ->
                                if (verifyTask.isSuccessful) {
                                    Toast.makeText(context, "Correo de verificación enviado a $correo", Toast.LENGTH_LONG).show()

                                    irAPantallaDeEspera()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }

    private fun irAPantallaDeEspera() {
        findNavController().navigate(R.id.action_paso2_to_verification)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
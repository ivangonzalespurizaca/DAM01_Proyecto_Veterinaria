package com.ivandev.proyectoveterinaria.fragment.registro

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.UsuarioDBHelper
import com.ivandev.proyectoveterinaria.databinding.FragmentVerificationBinding
import com.ivandev.proyectoveterinaria.viewmodel.RegistroClienteViewModel

class VerificationFragment : Fragment(R.layout.fragment_verification) {
    private val registroViewModel: RegistroClienteViewModel by activityViewModels()
    private var _binding: FragmentVerificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dbHelper: UsuarioDBHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVerificationBinding.bind(view)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        dbHelper = UsuarioDBHelper(requireContext())

        binding.btnConfirmarVerificacion.setOnClickListener {
            verificarEstadoDeCorreo()
        }

        binding.txtReenviarCorreo.setOnClickListener {
            reenviarEnlaceDeVerificacion()
        }
    }

    private fun verificarEstadoDeCorreo() {
        auth.currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val isVerified = auth.currentUser?.isEmailVerified == true

                if (isVerified) {
                    Toast.makeText(context, "¡Correo verificado con éxito!", Toast.LENGTH_SHORT).show()
                    finalizarRegistroEnBaseDeDatos()
                } else {
                    Toast.makeText(context, "Aún no has confirmado el enlace en tu correo.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Error al sincronizar: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reenviarEnlaceDeVerificacion() {
        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Se ha reenviado el enlace a tu bandeja de entrada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun finalizarRegistroEnBaseDeDatos() {
        val uid = auth.currentUser?.uid ?: ""
        val correo = auth.currentUser?.email ?: ""

        val usuario = registroViewModel.usuarioProceso.apply {
            this.id = uid
            this.correo = correo
        }

        firestore.collection("usuarios").document(uid)
            .set(usuario)
            .addOnSuccessListener {
                val resultado = dbHelper.insertarUsuario(usuario)

                if (resultado != -1L) {
                    Toast.makeText(context, "Registro completado exitosamente", Toast.LENGTH_LONG).show()
                    activity?.finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Fallo al guardar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
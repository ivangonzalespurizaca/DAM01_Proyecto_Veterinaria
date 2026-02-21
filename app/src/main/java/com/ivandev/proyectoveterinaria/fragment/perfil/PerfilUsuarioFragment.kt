package com.ivandev.proyectoveterinaria.fragment.perfil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.MainActivity
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentPerfilUsuarioBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Usuario

class PerfilUsuarioFragment : Fragment(R.layout.fragment_perfil_usuario), IFragmentoToolbar {
    override val titulo: String = "MI PERFIL"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PERFIL
    private var _binding: FragmentPerfilUsuarioBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val galerialauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            subirImagenACloudinary(it)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPerfilUsuarioBinding.bind(view)
        obtenerDatosUsuario()
        binding.btnCerrarSesion.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
        binding.btnCambiarFoto.setOnClickListener {
            galerialauncher.launch("image/*")
        }
        binding.btnCambiarContrasenia.setOnClickListener {
            mostrarDialogoRecuperacion()
        }

        binding. btnDatosPersonales.setOnClickListener {
            (activity as? PanelPrincipalActivity)?.replaceFragment(EditarDatosFragment())
        }
    }

    private fun obtenerDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                val usuario = document.toObject(Usuario::class.java)
                if (usuario != null) {
                    mostrarInformacion(usuario)
                }
            }
    }

    private fun mostrarInformacion(usuario: Usuario) {
        // 1. Cargar la foto de usuario con Glide (desde Cloudinary)
        if (!usuario.foto.isNullOrEmpty()) {
            Glide.with(this)
                .load(usuario.foto)
                .circleCrop()
                .placeholder(R.drawable.ic_perfil_usuario)
                .error(R.drawable.ic_perfil_usuario)
                .into(binding.ivFotoPerfil)
        }

        binding.tvNombrePerfil.text = usuario.nombreCompleto
        binding.tvCorreoPerfil.text = usuario.correo
        binding.tvRolBadge.text = usuario.rol?.uppercase()
    }

    private fun mostrarDialogoConfirmacion() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Cerrar Sesión")
        builder.setMessage("¿Estás seguro de que deseas salir de VetCareApp?")

        builder.setPositiveButton("Sí, salir") { _, _ ->
            cerrarSesionTotal()
        }

        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create()
        dialog.show()
    }

    private fun cerrarSesionTotal() {
        auth.signOut()

        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        requireActivity().finish()
    }

    private fun subirImagenACloudinary(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        MediaManager.get().upload(uri)
            .unsigned("vet_project")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) { }
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val urlSegura = resultData?.get("secure_url").toString()
                    actualizarFotoEnFirestore(urlSegura)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(requireContext(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) { }
            }).dispatch()
    }

    private fun actualizarFotoEnFirestore(url: String) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("usuarios").document(uid)
            .update("foto", url)
            .addOnSuccessListener {
                Glide.with(this).load(url).circleCrop().into(binding.ivFotoPerfil)
                Toast.makeText(requireContext(), "Foto actualizada", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoRecuperacion() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_recuperar_contrasenia, null)

        val etEmail = dialogView.findViewById<EditText>(R.id.etEmailRecuperacion)
        val btnEnviar = dialogView.findViewById<Button>(R.id.btnEnviarRecuperacion)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.btnCancelarRecuperacion)

        val correoActual = auth.currentUser?.email
        etEmail.setText(correoActual)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                enviarCorreoFirebase(email)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun enviarCorreoFirebase(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Enlace enviado a $email", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
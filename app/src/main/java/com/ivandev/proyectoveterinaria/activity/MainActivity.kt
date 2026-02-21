package com.ivandev.proyectoveterinaria.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Listeners principales
        binding.txtCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            loginUsuario()
        }

        binding.txtRecuperarContrasenia.setOnClickListener {
            mostrarDialogoRecuperacion()
        }
    }

    private fun loginUsuario() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    verificarRolYPermitirAcceso(user)
                }
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarRolYPermitirAcceso(user: com.google.firebase.auth.FirebaseUser) {
        firestore.collection("usuarios").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val rol = document.getString("rol") ?: "Cliente"
                    if (rol == "Cliente") {
                        if (user.isEmailVerified) {
                            redirigirAPanel(rol)
                        } else {
                            Toast.makeText(this, "Verifica tu correo electrónico", Toast.LENGTH_LONG).show()
                            auth.signOut()
                        }
                    } else {
                        redirigirAPanel(rol)
                    }
                }
            }
    }

    private fun mostrarDialogoRecuperacion() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_recuperar_contrasenia, null)

        val etEmail = dialogView.findViewById<EditText>(R.id.etEmailRecuperacion)
        val btnEnviar = dialogView.findViewById<Button>(R.id.btnEnviarRecuperacion)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.btnCancelarRecuperacion)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                enviarCorreoFirebase(email)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun enviarCorreoFirebase(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, "Enlace enviado a $email", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun redirigirAPanel(rol: String) {
        val intent = Intent(this, PanelPrincipalActivity::class.java)
        intent.putExtra("USER_ROLE", rol)
        startActivity(intent)
        finish()
    }
}
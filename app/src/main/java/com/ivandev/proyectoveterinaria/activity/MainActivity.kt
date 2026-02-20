package com.ivandev.proyectoveterinaria.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

        binding.txtCrearCuenta.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            loginUsuario()
        }
    }

    private fun loginUsuario() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if(user != null){
                    verificarRolYPermitirAcceso(user)
                }

//                if (user?.isEmailVerified == true) {
//                    obtenerRolYRedirigir(user.uid)
//                } else {
//                    Toast.makeText(this, "Por favor, verifica tu correo antes de entrar", Toast.LENGTH_LONG).show()
//                    auth.signOut()
//                }
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
                            Toast.makeText(this, "Por favor, verifica tu correo", Toast.LENGTH_LONG).show()
                            auth.signOut()
                        }
                    } else {
                        redirigirAPanel(rol)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al recuperar perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun redirigirAPanel(rol: String) {
        val intent = Intent(this, PanelPrincipalActivity::class.java)
        intent.putExtra("USER_ROLE", rol)
        startActivity(intent)
        finish()
    }
}
package com.ivandev.proyectoveterinaria.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ActivityPanelPrincipalBinding
import com.ivandev.proyectoveterinaria.fragment.PerfilUsuarioFragment
import com.ivandev.proyectoveterinaria.fragment.admin.catalogo.CatalogosFragment
import com.ivandev.proyectoveterinaria.fragment.admin.InicioAdminFragment
import com.ivandev.proyectoveterinaria.fragment.admin.ReportesFragment
import com.ivandev.proyectoveterinaria.fragment.admin.UsuariosFragment
import com.ivandev.proyectoveterinaria.fragment.cliente.EnAdopcionFragment
import com.ivandev.proyectoveterinaria.fragment.cliente.HistorialFragment
import com.ivandev.proyectoveterinaria.fragment.cliente.InicioClienteFragment
import com.ivandev.proyectoveterinaria.fragment.cliente.MascotasFragment
import com.ivandev.proyectoveterinaria.fragment.veterinario.AgendaFragment
import com.ivandev.proyectoveterinaria.fragment.veterinario.ConsultasFragment
import com.ivandev.proyectoveterinaria.fragment.veterinario.InicioVeterinarioFragment
import com.ivandev.proyectoveterinaria.fragment.veterinario.PacientesFragment
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Usuario

class PanelPrincipalActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPanelPrincipalBinding
    private var userRole: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    enum class TipoToolbar { PRINCIPAL, PERFIL, SECUNDARIO }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRole = intent.getStringExtra("USER_ROLE")

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        cargarDatosUsuario()

        try {
            val config = mapOf(
                "cloud_name" to "dfid8iuf3",
                "api_key" to "766956137225472",
                "api_secret" to "9ccrcCtEWoZygLXA149CUJ-eF_w"
            )
            MediaManager.init(this, config)
        } catch (e: Exception) { /* Ya inicializado */ }

        supportFragmentManager.addOnBackStackChangedListener {
            val fragmentActual = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            sincronizarToolbarAlRegresar(fragmentActual)
        }

        initToolbarViews()
        setupNavigation()
    }

    private fun initToolbarViews() {
        binding.toolbar.ivToolbarPerfil.setOnClickListener {
            replaceFragment(PerfilUsuarioFragment())
        }

        // Configurar la flecha atrás para que use el sistema de Android
        binding.toolbar.btnToolbarIzquierda.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupNavigation() {
        binding.bottomNavigation.menu.clear()

        // Configuración inicial por Rol
        val fragmentoInicial = when (userRole) {
            "Administrador" -> {
                binding.bottomNavigation.inflateMenu(R.menu.menu_administrador)
                InicioAdminFragment()
            }
            "Veterinario" -> {
                binding.bottomNavigation.inflateMenu(R.menu.menu_veterinario)
                InicioVeterinarioFragment()
            }
            else -> {
                binding.bottomNavigation.inflateMenu(R.menu.menu_cliente)
                InicioClienteFragment()
            }
        }

        replaceFragment(fragmentoInicial, false)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val inicio = when(userRole) {
                        "Administrador" -> InicioAdminFragment()
                        "Veterinario" -> InicioVeterinarioFragment()
                        else -> InicioClienteFragment()
                    }
                    replaceFragment(inicio, false)
                    true
                }
                // Administrador
                R.id.nav_catalogos -> { replaceFragment(CatalogosFragment()); true }
                R.id.nav_usuarios -> { replaceFragment(UsuariosFragment()); true }
                R.id.nav_reportes -> { replaceFragment(ReportesFragment()); true }

                // Veterinario
                R.id.nav_consultas -> { replaceFragment(ConsultasFragment()); true }
                R.id.nav_pacientes -> { replaceFragment(PacientesFragment()); true }
                R.id.nav_agenda -> { replaceFragment(AgendaFragment()); true }

                // Cliente
                R.id.nav_historial -> { replaceFragment(HistorialFragment()); true }
                R.id.nav_mascotas -> { replaceFragment(MascotasFragment()); true }
                R.id.nav_tratamientos -> { replaceFragment(EnAdopcionFragment()); true }

                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()

        transaction.replace(R.id.nav_host_fragment, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()

        val config = fragment as? IFragmentoToolbar
        configurarToolbarUI(config?.titulo, config?.tipo?: TipoToolbar.PRINCIPAL)
    }

    private fun configurarToolbarUI(titulo: String?, tipo: TipoToolbar) {
        binding.toolbar.tvToolbarTitulo.text = titulo
        when (tipo) {
            TipoToolbar.PRINCIPAL -> {
                binding.toolbar.btnToolbarIzquierda.visibility = View.GONE
                binding.toolbar.ivToolbarLogo.visibility = View.VISIBLE
                binding.toolbar.ivToolbarPerfil.visibility = View.VISIBLE
            }
            TipoToolbar.PERFIL -> {
                binding.toolbar.btnToolbarIzquierda.visibility = View.VISIBLE
                binding.toolbar.ivToolbarLogo.visibility = View.GONE
                binding.toolbar.ivToolbarPerfil.visibility = View.GONE
            }
            TipoToolbar.SECUNDARIO -> {
                binding.toolbar.btnToolbarIzquierda.visibility = View.VISIBLE
                binding.toolbar.ivToolbarLogo.visibility = View.GONE
                binding.toolbar.ivToolbarPerfil.visibility = View.VISIBLE
            }
        }
    }

    private fun sincronizarToolbarAlRegresar(fragment: Fragment?) {
        val configurable = fragment as? IFragmentoToolbar

        if (configurable != null) {
            configurarToolbarUI(configurable.titulo, configurable.tipo)
        } else {
            configurarToolbarUI("PANEL PRINCIPAL", TipoToolbar.PRINCIPAL)
        }
    }

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("usuarios").document(uid).get()
            .addOnSuccessListener { document ->
                val usuario = document.toObject(Usuario::class.java)
                if (usuario != null) {
                    actualizarInterfazCabecera(usuario)
                }
            }
    }

    private fun actualizarInterfazCabecera(usuario: Usuario) {
        if (!usuario.foto.isNullOrEmpty()) {
            Glide.with(this)
                .load(usuario.foto)
                .circleCrop()
                .placeholder(R.drawable.ic_perfil_usuario)
                .into(binding.toolbar.ivToolbarPerfil)
        }
    }


}
package com.ivandev.proyectoveterinaria.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ActivityPanelPrincipalBinding
import com.ivandev.proyectoveterinaria.fragment.admin.CatalogosFragment
import com.ivandev.proyectoveterinaria.fragment.admin.InicioAdminFragment
import com.ivandev.proyectoveterinaria.fragment.admin.ReportesFragment
import com.ivandev.proyectoveterinaria.fragment.admin.UsuariosFragment
import com.ivandev.proyectoveterinaria.fragment.cliente.HistorialFragment
import com.ivandev.proyectoveterinaria.fragment.cliente.InicioClienteFragment
import com.ivandev.proyectoveterinaria.fragment.cliente.MascotasFragment
import com.ivandev.proyectoveterinaria.fragment.cliente.TratamientosFragment
import com.ivandev.proyectoveterinaria.fragment.veterinario.AgendaFragment
import com.ivandev.proyectoveterinaria.fragment.veterinario.ConsultasFragment
import com.ivandev.proyectoveterinaria.fragment.veterinario.InicioVeterinarioFragment
import com.ivandev.proyectoveterinaria.fragment.veterinario.PacientesFragment

class PanelPrincipalActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPanelPrincipalBinding
    private var userRole: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRole = intent.getStringExtra("USER_ROLE")

        setupNavigation()
    }

    private fun setupNavigation() {
        binding.bottomNavigation.menu.clear()

        when (userRole) {
            "Administrador" -> {
                binding.bottomNavigation.inflateMenu(R.menu.menu_administrador)
                replaceFragment(InicioAdminFragment())
            }
            "Veterinario" -> {
                binding.bottomNavigation.inflateMenu(R.menu.menu_veterinario)
                replaceFragment(InicioVeterinarioFragment())
            }
            else -> {
                binding.bottomNavigation.inflateMenu(R.menu.menu_cliente)
                replaceFragment(InicioClienteFragment())
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val inicio = when(userRole) {
                        "Administrador" -> InicioAdminFragment()
                        "Veterinario" -> InicioVeterinarioFragment()
                        else -> InicioClienteFragment()
                    }
                    replaceFragment(inicio)
                    true
                }
                R.id.nav_historial -> {replaceFragment(HistorialFragment()); true}
                R.id.nav_mascotas -> {replaceFragment(MascotasFragment()); true}
                R.id.nav_tratamientos -> {replaceFragment(TratamientosFragment()); true}

                R.id.nav_consultas -> {replaceFragment(ConsultasFragment()); true}
                R.id.nav_pacientes -> {replaceFragment(PacientesFragment()); true}
                R.id.nav_agenda -> {replaceFragment(AgendaFragment()); true}

                R.id.nav_catalogos -> {replaceFragment(CatalogosFragment()); true}
                R.id.nav_usuarios -> {replaceFragment(UsuariosFragment()); true}
                R.id.nav_reportes -> {replaceFragment(ReportesFragment()); true}

                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }


}
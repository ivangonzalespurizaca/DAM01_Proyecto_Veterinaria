package com.ivandev.proyectoveterinaria.fragment.cliente

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.CarouselAdapter
import com.ivandev.proyectoveterinaria.adapter.MascotaClienteAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentInicioClienteBinding
import com.ivandev.proyectoveterinaria.fragment.admin.catalogo.VacunasListadoFragment
import com.ivandev.proyectoveterinaria.fragment.cliente.enAdopcion.EnAdopcionFragment
import com.ivandev.proyectoveterinaria.fragment.cliente.mascota.MascotasFragment
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.MascotaIntro
import com.ivandev.proyectoveterinaria.model.Usuario

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InicioClienteFragment : Fragment(R.layout.fragment_inicio_cliente), IFragmentoToolbar {
    override val titulo: String = "PANEL PRINCIPAL"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL
    private lateinit var viewPager: ViewPager2
    private var _binding: FragmentInicioClienteBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val sliderHandler = Handler(Looper.getMainLooper())


    private val misImagenes = listOf(
        R.drawable.carusel3,
        R.drawable.carusel2,
        R.drawable.carusel1
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentInicioClienteBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)
        mostrarFechaActual()
        cargarDatos()


        binding.tvVerTodasMascotas.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,  // Animación al entrar
                    R.anim.slide_out_left,  // Animación al salir lo anterior
                )
                .replace(R.id.nav_host_fragment, MascotasFragment())
                .addToBackStack(null) // <--- ESTO ES LA CLAVE
                .commit()
        }
        binding.Adopcion.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,  // Animación al entrar
                    R.anim.slide_out_left,  // Animación al salir lo anterior
                )
                .replace(R.id.nav_host_fragment, EnAdopcionFragment())
                .addToBackStack(null) // <--- ESTO ES LA CLAVE
                .commit()
        }
        binding.Vacunas.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                )
                .replace(R.id.nav_host_fragment, MascotasFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.citas.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                )
                .replace(R.id.nav_host_fragment, MascotasFragment())
                .addToBackStack(null)
                .commit()
        }




        viewPager = view.findViewById(R.id.viewPagerCarousel)
        val tabLayout: TabLayout = view.findViewById(R.id.tabLayoutDots)

        // Configurar Adaptador
        viewPager.adapter = CarouselAdapter(misImagenes)

        // Conectar puntitos (TabLayout)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        // EFECTO VISUAL: Animación de escalado al deslizar
        viewPager.setPageTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }

        // Registrar el callback para resetear el timer cuando el usuario toque el carrusel
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 5000) // 3 segundos
            }
        })
    }
    private val sliderRunnable = Runnable {
        val nextItem = if (viewPager.currentItem == misImagenes.size - 1) 0 else viewPager.currentItem + 1
        viewPager.currentItem = nextItem
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
    }


    private fun mostrarFechaActual() {
        val calendario = java.util.Calendar.getInstance().time
        val formato = java.text.SimpleDateFormat("EEE, d MMM", java.util.Locale("es", "PE"))

        // 3. Formateamos y aplicamos al TextView
        val fechaFormateada = formato.format(calendario)

        // Usamos .replaceFirstChar para que la primera letra sea Mayúscula (ej: "Mié" -> "Mié")
        binding.tvCurrentDate.text = "Hoy, ${fechaFormateada.replaceFirstChar { it.uppercase() }}"
    }


    private fun cargarDatos() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                val usuario = document.toObject(Usuario::class.java)
                binding.tvClientName.text = usuario?.nombreCompleto ?: "Bienvenido"

                // 3. ¡AQUÍ ES DONDE LLAMAS A LAS MASCOTAS!
                // Solo cuando ya sabemos que el usuario existe.
                cargarMascotasDelCliente(userId)
            }
    }
    private fun cargarMascotasDelCliente(clienteId: String) {
        db.collection("mascotas")
            .whereEqualTo("idCliente", clienteId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val listaMascotas = snapshot?.toObjects(MascotaIntro::class.java) ?: listOf()

                // 4. Pasamos la lista al RecyclerView
                configurarRecyclerViewMascotas(listaMascotas)
            }
    }

    private fun configurarRecyclerViewMascotas(lista: List<MascotaIntro>) {
        val adapter = MascotaClienteAdapter(lista)
        binding.rvMisMascotas.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
    }

}
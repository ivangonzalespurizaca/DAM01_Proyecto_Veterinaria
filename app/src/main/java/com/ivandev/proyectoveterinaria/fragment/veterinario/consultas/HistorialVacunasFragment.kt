package com.ivandev.proyectoveterinaria.fragment.veterinario.consultas

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.VacunaAplicadaAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentHistorialVacunasBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Mascota
import com.ivandev.proyectoveterinaria.model.MascotaAdopcion
import com.ivandev.proyectoveterinaria.model.VacunaAplicada
import com.ivandev.proyectoveterinaria.viewmodel.VacunaAplicadaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistorialVacunasFragment : Fragment(R.layout.fragment_historial_vacunas), IFragmentoToolbar {
    override val titulo: String = "HISTORIAL DE VACUNAS"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO
    private lateinit var binding: FragmentHistorialVacunasBinding
    private val viewModel: VacunaAplicadaViewModel by viewModels()
    private lateinit var vacunaAdapter: VacunaAplicadaAdapter

    // Variable local para el ID
    private var idMascota: String? = null
    private var mascota: Mascota? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHistorialVacunasBinding.bind(view)

        mascota = arguments?.let{
            BundleCompat.getParcelable(it, "mascota", Mascota::class.java)
        }

        // 1. Recuperar el ID del Bundle manualmente
        idMascota = mascota?.idMascota

        setupRecyclerView()
        setupSearch()
        setupObservers()

        // 2. Navegación manual al registro
        binding.fabAgregarVacuna.setOnClickListener {
            abrirRegistroVacuna()
        }

        // 3. Cargar datos si el ID existe
        idMascota?.let { id ->
            viewModel.listarVacunasPorMascota(id)
        } ?: run {
            Toast.makeText(requireContext(), "Error: No se encontró el ID de la mascota", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirRegistroVacuna() {
        // Creamos el fragmento y le pasamos el ID en un Bundle
        val fragmentoRegistro = RegistrarAplicacionVacunaFragment()
        val bundle = Bundle().apply {
            putParcelable("mascota", mascota)
        }
        fragmentoRegistro.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragmentoRegistro) // 'container_main' debe ser el ID en tu Activity
            .addToBackStack(null)
            .commit()
    }

    private fun setupRecyclerView() {
        vacunaAdapter = VacunaAplicadaAdapter { vacuna, anchorView ->
            mostrarMenuOpciones(vacuna, anchorView)
        }
        binding.rvVacunas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = vacunaAdapter
        }
    }

    private fun setupObservers() {
        viewModel.listaHistorial.observe(viewLifecycleOwner) { lista ->
            vacunaAdapter.updateList(lista)
        }
    }

    private fun setupSearch() {
        binding.etBuscarVacuna.addTextChangedListener { texto ->
            vacunaAdapter.filtrar(texto.toString())
        }
    }

    private fun mostrarMenuOpciones(vacuna: VacunaAplicada, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_vacunas, popup.menu)

        // Buscamos los items en el menú
        val itemEliminar = popup.menu.findItem(R.id.action_eliminar)
        val itemAplicar = popup.menu.findItem(R.id.action_aplicar)
        val itemDetalles = popup.menu.findItem(R.id.action_detalles)
        val itemEditar = popup.menu.findItem(R.id.action_editar)

        // Lógica de Visibilidad según el estado
        if (vacuna.estado == "Pendiente") {
            itemEliminar.isVisible = true
            itemAplicar.isVisible = true
            itemDetalles.isVisible = true // Opcional, si quieres que siempre se vea
            itemEditar.isVisible = false
        } else {
            // Si ya está "Aplicada", ocultamos las acciones de gestión
            itemEliminar.isVisible = false
            itemAplicar.isVisible = false
            itemDetalles.isVisible = true
            itemEditar.isVisible = true
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_eliminar -> {
                    mostrarConfirmacion(
                        titulo = "Eliminar Registro",
                        mensaje = "¿Estás seguro de eliminar la vacuna ${vacuna.nombreVacuna}? Esta acción no se puede deshacer.",
                        icono = R.drawable.ic_power
                    ) {
                        confirmarEliminacion(vacuna)
                    }
                    true
                }
                R.id.action_aplicar -> {
                    mostrarConfirmacion(
                        titulo = "Confirmar Aplicación",
                        mensaje = "¿Deseas marcar la vacuna ${vacuna.nombreVacuna} como aplicada hoy?",
                        icono = R.drawable.ic_vaccine
                    ) {
                        aplicarVacunaDirecto(vacuna)
                    }
                    true
                }
                R.id.action_detalles -> {
                    mostrarDetallesDialog(vacuna)
                    true
                }
                R.id.action_editar -> {
                    abrirEdicionVacuna(vacuna)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun abrirEdicionVacuna(vacuna: VacunaAplicada) {
        val fragmento = RegistrarAplicacionVacunaFragment()
        val bundle = Bundle().apply {
            putParcelable("mascota", mascota)
            putParcelable("vacunaEditar", vacuna)
        }
        fragmento.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragmento)
            .addToBackStack(null)
            .commit()
    }

    private fun mostrarConfirmacion(titulo: String, mensaje: String, icono: Int, accion: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setMessage(mensaje)
            .setIcon(icono)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ ->
                accion()
            }
            .show()
    }

    private fun mostrarDetallesDialog(vacuna: VacunaAplicada) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_detalle_vacuna, null)

        // Referencias de las vistas del layout personalizado
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvDetalleTitulo)
        val tvDosis = dialogView.findViewById<TextView>(R.id.tvDetalleDosis)
        val tvFecha = dialogView.findViewById<TextView>(R.id.tvDetalleFecha)
        val tvObs = dialogView.findViewById<TextView>(R.id.tvDetalleObservaciones)
        val ivEstado = dialogView.findViewById<ImageView>(R.id.ivDetalleEstadoIcono)

        // Seteo de datos
        tvTitulo.text = vacuna.nombreVacuna
        tvDosis.text = "Dosis número: ${vacuna.nroDosis}"
        tvObs.text = if (vacuna.observaciones.isNullOrEmpty()) "Sin notas adicionales." else vacuna.observaciones

        // Lógica visual por estado
        if (vacuna.estado == "Pendiente") {
            tvFecha.text = "Programada para: ${vacuna.fechaProgramada}"
            ivEstado.setImageResource(R.drawable.ic_clock)
            ivEstado.imageTintList = ColorStateList.valueOf(requireContext().getColor(R.color.brand_orange))
            tvTitulo.setTextColor(requireContext().getColor(R.color.brand_orange))
        } else {
            tvFecha.text = "Aplicada el: ${vacuna.fechaAplicacion}"
            ivEstado.setImageResource(R.drawable.ic_vaccine)
            ivEstado.imageTintList = ColorStateList.valueOf(requireContext().getColor(R.color.brand_green))
            tvTitulo.setTextColor(requireContext().getColor(R.color.brand_green))
        }

        builder.setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun aplicarVacunaDirecto(vacuna: VacunaAplicada) {
        val hoy = SimpleDateFormat("yyyy/dd/MM", Locale.getDefault()).format(Date())

        val vacunaActualizada = vacuna.copy(
            estado = "Aplicada",
            fechaAplicacion = hoy
        )

        // 3. Llamamos al método guardarVacuna del ViewModel
        viewModel.guardarVacuna(vacunaActualizada) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "¡Vacuna aplicada con éxito!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error al actualizar en la nube", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarEliminacion(vacuna: VacunaAplicada) {
        viewModel.eliminarVacuna(vacuna) { exito ->
            if (exito) Toast.makeText(requireContext(), "Cita eliminada", Toast.LENGTH_SHORT).show()
        }
    }
}
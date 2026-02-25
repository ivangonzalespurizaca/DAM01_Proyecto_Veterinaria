package com.ivandev.proyectoveterinaria.fragment.veterinario.solicitud

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.SolicitudVetAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentSolicitudBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.SolicitudAdopcion
import com.ivandev.proyectoveterinaria.viewmodel.MascotaAdopcionViewModel

class SolicitudFragment : Fragment(R.layout.fragment_solicitud), IFragmentoToolbar {

    override val titulo: String = "SOLICITUDES"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentSolicitudBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MascotaAdopcionViewModel by viewModels()
    private lateinit var adapter: SolicitudVetAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSolicitudBinding.bind(view)

        setupRecyclerView()
        setupFiltros()
        observeViewModel()

        viewModel.cargarSolicitudes()
    }

    private fun setupRecyclerView() {
        adapter = SolicitudVetAdapter(
            solicitudes = emptyList(),
            onVerDetalles = { solicitud ->
                mostrarDialogoDetalle(solicitud)
            },
            onGestionar = { solicitud ->
                mostrarDialogoGestion(solicitud)
            }
        )
        binding.rvMascotasAdopcion.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@SolicitudFragment.adapter
        }
    }

    private fun setupFiltros() {
        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, checkedIds ->
            val estado = when (checkedIds.firstOrNull()) {
                R.id.chipPendientes -> "Pendiente"
                R.id.chipProceso -> "En Proceso"
                R.id.chipAprobadas -> "Aprobada"
                else -> "Todos"
            }
            viewModel.filtrarPorEstado(estado) //
        }
    }

    private fun observeViewModel() {
        viewModel.solicitudes.observe(viewLifecycleOwner) { lista ->
            adapter.actualizarLista(lista)
        }
    }

    private fun mostrarDialogoGestion(solicitud: SolicitudAdopcion) {
        val builder = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
        when (solicitud.estado) {
            "Pendiente" -> {
                val view = layoutInflater.inflate(R.layout.dialog_gestion_solicitud, null)
                val etFecha = view.findViewById<EditText>(R.id.etFechaGestion)
                val etMensaje = view.findViewById<EditText>(R.id.etMensajeGestion)

                etFecha.setOnClickListener {
                    val cal = java.util.Calendar.getInstance()
                    android.app.DatePickerDialog(requireContext(), { _, anio, mes, dia ->
                        val fechaISO = String.format("%04d/%02d/%02d", anio, mes + 1, dia) //
                        etFecha.setText(fechaISO)
                    }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).apply {
                        datePicker.minDate = System.currentTimeMillis()
                        show()
                    }
                }

                builder.setTitle("PROGRAMAR CITA")
                    .setView(view)
                    .setPositiveButton("AGENDAR") { _, _ ->
                        val fecha = etFecha.text.toString()
                        if (fecha.isNotEmpty()) {
                            ejecutarAccion(solicitud, "En Proceso", etMensaje.text.toString(), fecha)
                        } else {
                            Toast.makeText(context, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("CANCELAR", null)
                    .show()
            }

            "En Proceso" -> {
                builder.setTitle("VEREDICTO DE ADOPCIÓN")
                    .setMessage("¿Deseas aprobar la adopción de ${solicitud.nombreMascota}?")
                    .setPositiveButton("APROBAR") { _, _ ->
                        ejecutarAccion(solicitud, "Aprobada", "¡Felicidades! Adopción aprobada.")
                    }
                    .setNegativeButton("DECLINAR") { _, _ ->
                        ejecutarAccion(solicitud, "Declinada", "Solicitud declinada tras entrevista.")
                    }
                    .setNeutralButton("VOLVER", null)
                    .show()
            }
        }
    }

    private fun mostrarDialogoDetalle(solicitud: SolicitudAdopcion) {
        val view = layoutInflater.inflate(R.layout.dialog_detalle_solicitud, null)

        // Referencias
        val ivFoto = view.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.ivDetalleFoto)
        val tvMascota = view.findViewById<TextView>(R.id.tvDetalleMascota)
        val tvCliente = view.findViewById<TextView>(R.id.tvDetalleCliente)
        val tvTelefono = view.findViewById<TextView>(R.id.tvDetalleTelefono)
        val tvFechaCreacion = view.findViewById<TextView>(R.id.tvDetalleFechaCreacion)
        val tvCita = view.findViewById<TextView>(R.id.tvDetalleCita)
        val tvMensaje = view.findViewById<TextView>(R.id.tvDetalleMensaje)

        // Llenado de datos
        tvMascota.text = solicitud.nombreMascota
        tvCliente.text = solicitud.nombreCliente
        tvTelefono.text = solicitud.telefonoCliente

        // Formateo de fecha de solicitud
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        tvFechaCreacion.text = "Enviada: ${sdf.format(solicitud.fecha.toDate())}"

        tvCita.text = if (solicitud.fechaEntrevista.isEmpty()) "Cita: Aún no programada"
        else "Fecha de Cita: ${solicitud.fechaEntrevista}"

        if (solicitud.mensajeVet.isEmpty()) {
            view.findViewById<View>(R.id.tvLabelMensaje).visibility = View.GONE
            tvMensaje.visibility = View.GONE
        } else {
            tvMensaje.text = solicitud.mensajeVet
        }

        Glide.with(requireContext()).load(solicitud.fotoMascota).circleCrop().into(ivFoto)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(view)
            .setPositiveButton("CERRAR", null)
            .show()
    }

    private fun ejecutarAccion(solicitud: SolicitudAdopcion, estado: String, msg: String, fecha: String = "") {
        viewModel.actualizarEstado(solicitud, estado, msg, fecha) { exito ->
            if (exito) {
                val aviso = if(estado == "Aprobada") "¡Mascota Adoptada!" else "Estado: $estado"
                Toast.makeText(context, aviso, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
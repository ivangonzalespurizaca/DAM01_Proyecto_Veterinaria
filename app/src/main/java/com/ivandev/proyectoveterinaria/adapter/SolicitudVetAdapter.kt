package com.ivandev.proyectoveterinaria.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemSolicitudAdminBinding
import com.ivandev.proyectoveterinaria.model.SolicitudAdopcion

class SolicitudVetAdapter(
    private var solicitudes: List<SolicitudAdopcion>,
    private val onVerDetalles: (SolicitudAdopcion) -> Unit,
    private val onGestionar: (SolicitudAdopcion) -> Unit
) : RecyclerView.Adapter<SolicitudVetAdapter.SolicitudViewHolder>() {

    inner class SolicitudViewHolder(val binding: ItemSolicitudAdminBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitudViewHolder {
        val binding = ItemSolicitudAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SolicitudViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SolicitudViewHolder, position: Int) {
        val solicitud = solicitudes[position]
        val context = holder.itemView.context

        holder.binding.apply {
            // 1. Cargar imagen y textos básicos
            tvSolicitudMascotaNombre.text = solicitud.nombreMascota
            tvSolicitudClienteNombre.text = "Interesado: ${solicitud.nombreCliente}"

            Glide.with(context)
                .load(solicitud.fotoMascota)
                .placeholder(R.drawable.ic_pet)
                .into(ivSolicitudMascota)

            // 2. Lógica de Fecha de Cita
            if (solicitud.fechaEntrevista.isEmpty()) {
                tvSolicitudFechaCita.text = "CITA: No asignado"
                tvSolicitudFechaCita.alpha = 0.6f
            } else {
                tvSolicitudFechaCita.text = "CITA: ${solicitud.fechaEntrevista}"
                tvSolicitudFechaCita.alpha = 1.0f
            }

            // 3. Badge de Estado con colores dinámicos
            tvSolicitudEstado.text = solicitud.estado.uppercase()
            val colorRes = when (solicitud.estado) {
                "Pendiente" -> R.color.text_muted
                "En Proceso" -> R.color.brand_orange
                "Aprobada" -> R.color.brand_green
                "Rechazada", "Declinada" -> R.color.error_red //
                else -> R.color.text_muted
            }
            tvSolicitudEstado.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, colorRes)
            )

            // --- LÓGICA CONTEXTUAL PARA ESTADOS FINALES ---
            val esEstadoFinal = solicitud.estado == "Aprobada" ||
                    solicitud.estado == "Rechazada" ||
                    solicitud.estado == "Declinada"

            // Cambiamos el icono para dar una pista visual al Vet
            if (esEstadoFinal) {
                btnGestionarSolicitud.setImageResource(R.drawable.ic_info_outline)
            } else {
                btnGestionarSolicitud.setImageResource(R.drawable.ic_more_vert)
            }

            // 4. Menú de Opciones Contextual
            btnGestionarSolicitud.setOnClickListener { view ->
                val popup = PopupMenu(context, view)
                popup.menuInflater.inflate(R.menu.menu_solicitud_veterinario, popup.menu)

                // OCULTAMOS "GESTIONAR" SI YA TERMINÓ
                popup.menu.findItem(R.id.action_gestionar)?.isVisible = !esEstadoFinal

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_ver_detalles -> { onVerDetalles(solicitud); true }
                        R.id.action_gestionar -> { onGestionar(solicitud); true }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    override fun getItemCount(): Int = solicitudes.size

    fun actualizarLista(nuevaLista: List<SolicitudAdopcion>) {
        this.solicitudes = nuevaLista
        notifyDataSetChanged()
    }
}
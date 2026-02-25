package com.ivandev.proyectoveterinaria.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemSolicitudClienteBinding
import com.ivandev.proyectoveterinaria.model.SolicitudAdopcion

class MisSolicitudesAdapter(
    private var solicitudes: List<SolicitudAdopcion>
) : RecyclerView.Adapter<MisSolicitudesAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSolicitudClienteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSolicitudClienteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val solicitud = solicitudes[position]
        val context = holder.itemView.context

        holder.binding.apply {
            // 1. Información de la Mascota
            tvMisSolicitudMascotaNombre.text = solicitud.nombreMascota

            Glide.with(context)
                .load(solicitud.fotoMascota)
                .placeholder(R.drawable.ic_pet)
                .into(ivMisSolicitudMascota)

            // 2. Estado con Colores Semánticos
            tvMisSolicitudEstado.text = solicitud.estado.uppercase()

            val colorRes = when (solicitud.estado) {
                "Pendiente" -> R.color.text_muted      // Gris
                "En Proceso" -> R.color.brand_orange   // Naranja
                "Aprobada" -> R.color.brand_green      // Verde
                "Declinada", "Rechazada" -> R.color.error_red // Rojo
                else -> R.color.text_muted
            }

            tvMisSolicitudEstado.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, colorRes)
            )

            // 3. Respuesta de la Clínica (Cita y Mensaje)
            tvMisSolicitudCita.text = if (solicitud.fechaEntrevista.isEmpty()) {
                "Cita: Pendiente de asignar"
            } else {
                "Cita Agendada: ${solicitud.fechaEntrevista}"
            }
            tvMisSolicitudMensaje.text = solicitud.mensajeVet.takeIf { it.isNotEmpty() }
                ?: "Estamos evaluando tu perfil. ¡Pronto nos contactaremos!"
        }
    }

    override fun getItemCount(): Int = solicitudes.size

    fun actualizarLista(nuevaLista: List<SolicitudAdopcion>) {
        this.solicitudes = nuevaLista
        notifyDataSetChanged()
    }
}
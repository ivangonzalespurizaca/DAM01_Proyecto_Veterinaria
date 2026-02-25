package com.ivandev.proyectoveterinaria.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemMascotaAdopcionClienteBinding
import com.ivandev.proyectoveterinaria.model.MascotaAdopcion

class MascotaAdopcionClienteAdapter(
    private var listaMascotas: List<MascotaAdopcion>,
    private val onDetalles: (MascotaAdopcion) -> Unit,
    private val onSolicitar: (MascotaAdopcion) -> Unit
) : RecyclerView.Adapter<MascotaAdopcionClienteAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMascotaAdopcionClienteBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMascotaAdopcionClienteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mascota = listaMascotas[position]

        holder.binding.apply {
            // 1. Llenado de datos (Usando campos desnormalizados)
            tvNombreAdopcion.text = mascota.nombreMascota
            tvRazaEspecieAdopcion.text = "${mascota.nombreRaza} - ${mascota.nombreEspecie}"
            tvDetallesAdopcion.text = "${mascota.sexo} • ${mascota.edadEstimada}"

            // 2. Estado Visual
            tvEstadoAdopcion.text = mascota.estado.uppercase()
            if (mascota.estado != "Disponible") {
                tvEstadoAdopcion.backgroundTintList = ContextCompat.getColorStateList(root.context, R.color.brand_orange)
            }

            // 3. Carga de Imagen
            Glide.with(root.context)
                .load(mascota.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoAdopcion)

            // 4. Configuración del Menú de 3 Puntos
            btnMenuOpciones.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.menu_item_adopcion, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.optionVerDetalles -> {
                            onDetalles(mascota)
                            true
                        }
                        R.id.optionSolicitud -> {
                            onSolicitar(mascota)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }

    override fun getItemCount(): Int = listaMascotas.size

    fun actualizarLista(nuevaLista: List<MascotaAdopcion>) {
        this.listaMascotas = nuevaLista
        notifyDataSetChanged()
    }
}
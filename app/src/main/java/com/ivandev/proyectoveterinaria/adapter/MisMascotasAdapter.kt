package com.ivandev.proyectoveterinaria.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemMascotaBusquedaBinding
import com.ivandev.proyectoveterinaria.model.Mascota

class MisMascotasAdapter(
    private val onMascotaClick: (Mascota) -> Unit
) : RecyclerView.Adapter<MisMascotasAdapter.MascotaViewHolder>() {

    private var listaMascotas: List<Mascota> = emptyList()

    // Función para cargar los datos desde el Fragment/ViewModel
    fun updateList(nuevaLista: List<Mascota>) {
        listaMascotas = nuevaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val binding = ItemMascotaBusquedaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MascotaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = listaMascotas[position]
        val context = holder.itemView.context

        holder.binding.apply {
            // Seteamos el nombre y la combinación Raza - Especie
            tvNombreBusqueda.text = mascota.nombreMascota
            tvRazaBusqueda.text = "${mascota.nombreRaza} - ${mascota.nombreEspecie}"

            // Cargamos la foto con Glide usando el estilo circular que definimos
            Glide.with(context)
                .load(mascota.foto)
                .placeholder(R.drawable.ic_pet)
                .centerCrop()
                .into(ivFotoBusqueda)

            // Detectamos el clic en toda la tarjeta para ir al historial
            root.setOnClickListener { onMascotaClick(mascota) }
        }
    }

    override fun getItemCount(): Int = listaMascotas.size

    class MascotaViewHolder(val binding: ItemMascotaBusquedaBinding) :
        RecyclerView.ViewHolder(binding.root)
}
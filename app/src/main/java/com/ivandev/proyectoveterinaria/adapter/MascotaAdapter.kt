package com.ivandev.proyectoveterinaria.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemMascotaBinding
import com.ivandev.proyectoveterinaria.model.Mascota

class MascotaAdapter(
    private var lista: MutableList<Mascota>,
    private var especiesMap: Map<String, String> = emptyMap(),
    private var razasMap: Map<String, String> = emptyMap(),
    private val onOpcionSeleccionada: (Mascota, Int) -> Unit
) : RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    class MascotaViewHolder(val binding: ItemMascotaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val binding = ItemMascotaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MascotaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = lista[position]
        holder.binding.apply {
            tvNombreMascota.text = mascota.nombreMascota

            // Traducimos IDs a nombres reales
            val nombreEspecie = especiesMap[mascota.idEspecie] ?: "Desconocida"
            val nombreRaza = razasMap[mascota.idRaza] ?: "Desconocida"
            tvRazaMascota.text = "$nombreRaza - $nombreEspecie"

            // Detalles clínicos concatenados
            tvDetallesMascota.text = "${mascota.sexo} • ${mascota.pesoInicial} kg"

            Glide.with(root.context)
                .load(mascota.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoMascota)

            // Menú de opciones (Overflow)
            btnOpcionesMascota.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.inflate(R.menu.menu_item_mascota)

                popup.setOnMenuItemClickListener { item ->
                    onOpcionSeleccionada(mascota, item.itemId)
                    true
                }
                popup.show()
            }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Mascota>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    // Función para actualizar ambos mapas de catálogos
    fun actualizarMapas(nuevasEspecies: Map<String, String>, nuevasRazas: Map<String, String>) {
        this.especiesMap = nuevasEspecies
        this.razasMap = nuevasRazas
        notifyDataSetChanged()
    }
}
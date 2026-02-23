package com.ivandev.proyectoveterinaria.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemMascotaBusquedaBinding
import com.ivandev.proyectoveterinaria.model.Mascota

class MascotaBusquedaAdapter(
    private var lista: MutableList<Mascota>,
    private var especiesMap: Map<String, String>,
    private var razasMap: Map<String, String>,
    private val onMascotaSelected: (Mascota) -> Unit
) : RecyclerView.Adapter<MascotaBusquedaAdapter.BusquedaViewHolder>() {

    class BusquedaViewHolder(val binding: ItemMascotaBusquedaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusquedaViewHolder {
        val binding = ItemMascotaBusquedaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BusquedaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BusquedaViewHolder, position: Int) {
        val mascota = lista[position]
        holder.binding.apply {
            tvNombreBusqueda.text = mascota.nombreMascota

            val especie = especiesMap[mascota.idEspecie] ?: "Desconocida"
            val raza = razasMap[mascota.idRaza] ?: "Desconocida"
            tvRazaBusqueda.text = "$raza - $especie"

            Glide.with(root.context)
                .load(mascota.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoBusqueda)

            root.setOnClickListener { onMascotaSelected(mascota) }
        }
    }

    override fun getItemCount() = lista.size

    fun actualizarLista(nuevaLista: List<Mascota>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
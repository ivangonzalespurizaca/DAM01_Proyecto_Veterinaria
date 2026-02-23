package com.ivandev.proyectoveterinaria.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ivandev.proyectoveterinaria.databinding.ItemRazaBinding
import com.ivandev.proyectoveterinaria.model.Raza

class RazaAdapter(
    private var listaRazas: MutableList<Raza>,
    private var mapaEspecies: Map<String, String> = emptyMap(),
    private val onGestionar: (Raza) -> Unit
) : RecyclerView.Adapter<RazaAdapter.RazaViewHolder>() {

    class RazaViewHolder(val binding: ItemRazaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RazaViewHolder {
        val binding = ItemRazaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RazaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RazaViewHolder, position: Int) {
        val raza = listaRazas[position]

        holder.binding.apply {
            tvNombreRaza.text = raza.nombre
            tvIdRaza.text = "ID: ${raza.id}"

            val nombreEspecie = mapaEspecies[raza.idEspecie] ?: "Sin especie"
            tvEspecieAsociada.text = "Especie: $nombreEspecie"

            root.setOnClickListener { onGestionar(raza) }
        }
    }

    override fun getItemCount(): Int = listaRazas.size

    // Métodos para actualizar los datos desde el Fragment
    fun actualizarLista(nuevaLista: List<Raza>) {
        listaRazas.clear()
        listaRazas.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun actualizarMapaEspecies(nuevoMapa: Map<String, String>) {
        mapaEspecies = nuevoMapa
        notifyDataSetChanged()
    }
}
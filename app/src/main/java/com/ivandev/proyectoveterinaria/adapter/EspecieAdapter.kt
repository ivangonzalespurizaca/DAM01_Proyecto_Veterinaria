package com.ivandev.proyectoveterinaria.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ivandev.proyectoveterinaria.databinding.ItemEspecieBinding
import com.ivandev.proyectoveterinaria.model.Especie

class EspecieAdapter(
    private var lista: MutableList<Especie>,
    private val onGestionar: (Especie) -> Unit
) : RecyclerView.Adapter<EspecieAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemEspecieBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEspecieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val especie = lista[position]
        holder.binding.apply {
            tvNombreCatalogo.text = especie.nombre
            tvDetalleCatalogo.text = especie.definicion
            tvIdCatalogo.text = "ID: ${especie.id}"

            // Al tocar la tarjeta, abrimos las opciones
            cardItemCatalogo.setOnClickListener { onGestionar(especie) }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Especie>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
package com.ivandev.proyectoveterinaria.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ivandev.proyectoveterinaria.databinding.ItemVacunaBinding
import com.ivandev.proyectoveterinaria.model.Vacuna

class VacunaAdapter(
    private var lista: MutableList<Vacuna>,
    private val onGestionar: (Vacuna) -> Unit
) : RecyclerView.Adapter<VacunaAdapter.VacunaViewHolder>() {

    class VacunaViewHolder(val binding: ItemVacunaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacunaViewHolder {
        val binding = ItemVacunaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VacunaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VacunaViewHolder, position: Int) {
        val vacuna = lista[position]
        holder.binding.apply {
            tvNombreVacunaCatalogo.text = vacuna.nombreVacuna
            tvIdVacunaCatalogo.text = "ID: ${vacuna.idVacuna}"

            root.setOnClickListener { onGestionar(vacuna) }
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<Vacuna>) {
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
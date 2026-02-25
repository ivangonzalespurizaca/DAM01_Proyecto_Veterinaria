package com.ivandev.proyectoveterinaria.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemVacunaMiMascotaBinding
import com.ivandev.proyectoveterinaria.model.VacunaAplicada

class VacunaMiMascotaAdapter(
    private val onVacunaClick: (VacunaAplicada) -> Unit
) : RecyclerView.Adapter<VacunaMiMascotaAdapter.VacunaViewHolder>() {

    private var listaVacunas = listOf<VacunaAplicada>()
    private var listaFiltrada = listOf<VacunaAplicada>()

    fun updateList(nuevaLista: List<VacunaAplicada>) {
        listaVacunas = nuevaLista
        listaFiltrada = nuevaLista
        notifyDataSetChanged()
    }

    fun filtrar(query: String) {
        listaFiltrada = if (query.isEmpty()) listaVacunas
        else {
            listaVacunas.filter { it.nombreVacuna.lowercase().contains(query.lowercase()) }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacunaViewHolder {
        val binding = ItemVacunaMiMascotaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VacunaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VacunaViewHolder, position: Int) {
        val vacuna = listaFiltrada[position]
        holder.bind(vacuna, onVacunaClick)
    }

    override fun getItemCount(): Int = listaFiltrada.size

    class VacunaViewHolder(private val binding: ItemVacunaMiMascotaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(vacuna: VacunaAplicada, onVacunaClick: (VacunaAplicada) -> Unit) {
            val context = binding.root.context

            binding.tvNombreVacuna.text = vacuna.nombreVacuna
            binding.tvNumeroDosis.text = "Dosis: ${vacuna.nroDosis}"

            // Lógica de colores y fechas según estado
            if (vacuna.estado == "Aplicada") {
                binding.tvEstadoVacuna.text = "APLICADA"
                binding.tvEstadoVacuna.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.brand_green))
                binding.tvFechaDinamica.text = "Aplicada el: ${vacuna.fechaAplicacion}"
                binding.ivIconoVacuna.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.brand_green))
            } else {
                binding.tvEstadoVacuna.text = "PENDIENTE"
                binding.tvEstadoVacuna.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.brand_orange))
                binding.tvFechaDinamica.text = "Programada: ${vacuna.fechaProgramada}"
                binding.ivIconoVacuna.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.brand_orange))
            }

            binding.root.setOnClickListener { onVacunaClick(vacuna) }
        }
    }
}
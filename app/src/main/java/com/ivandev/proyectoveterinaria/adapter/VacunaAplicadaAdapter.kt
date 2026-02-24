package com.ivandev.proyectoveterinaria.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemVacunaAplicadaBinding
import com.ivandev.proyectoveterinaria.model.VacunaAplicada

class VacunaAplicadaAdapter(
    private val onOptionClick: (VacunaAplicada, View) -> Unit
) : RecyclerView.Adapter<VacunaAplicadaAdapter.VacunaViewHolder>() {

    private var listaOriginal: List<VacunaAplicada> = emptyList()
    private var listaFiltrada: List<VacunaAplicada> = emptyList()

    fun updateList(nuevaLista: List<VacunaAplicada>) {
        listaOriginal = nuevaLista
        listaFiltrada = nuevaLista
        notifyDataSetChanged()
    }

    // Lógica para el buscador (etBuscarVacuna)
    fun filtrar(texto: String) {
        listaFiltrada = if (texto.isEmpty()) {
            listaOriginal
        } else {
            listaOriginal.filter { it.nombreVacuna.contains(texto, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VacunaViewHolder {
        val binding = ItemVacunaAplicadaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VacunaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VacunaViewHolder, position: Int) {
        holder.bind(listaFiltrada[position])
    }

    override fun getItemCount(): Int = listaFiltrada.size

    inner class VacunaViewHolder(private val binding: ItemVacunaAplicadaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(vacuna: VacunaAplicada) {
            val context = binding.root.context
            binding.tvNombreVacuna.text = vacuna.nombreVacuna
            binding.tvNumeroDosis.text = "Dosis: ${vacuna.nroDosis}"
            binding.tvEstadoVacuna.text = if(vacuna.estado == "Pendiente") "PENDIENTE" else "APLICADA"

// 2. Lógica de Colores y Estilos según el Estado
            if (vacuna.estado == "Pendiente") {
                // --- ESTADO PENDIENTE (Naranja/Alerta) ---
                binding.tvEstadoVacuna.text = "PENDIENTE"

                // Icono de espera/reloj
                //binding.ivIconoVacuna.setImageResource(R.drawable.ic_clock)
                binding.ivIconoVacuna.imageTintList = ColorStateList.valueOf(context.getColor(R.color.brand_orange))
                binding.flIconoContainer.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.brand_orange_light))
                binding.tvEstadoVacuna.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.brand_orange)
                )
                binding.tvFechaDinamica.text = "Cita: ${vacuna.fechaProgramada}"
                binding.tvNumeroDosis.setTextColor(context.getColor(R.color.brand_orange))

            } else {
                // --- ESTADO APLICADA (Verde/Éxito) ---
                binding.tvEstadoVacuna.text = "APLICADA"
                binding.tvEstadoVacuna.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.brand_green))

                // Icono de vacuna/check
                binding.ivIconoVacuna.setImageResource(R.drawable.ic_vaccine)
                binding.ivIconoVacuna.imageTintList = ColorStateList.valueOf(context.getColor(R.color.brand_green))
                binding.flIconoContainer.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.brand_green_light))

                binding.tvFechaDinamica.text = "Aplicada: ${vacuna.fechaAplicacion}"
                binding.tvNumeroDosis.setTextColor(context.getColor(R.color.brand_blue))
            }

            // El botón de los 3 puntos
            binding.btnMenuOpciones.setOnClickListener {
                onOptionClick(vacuna, it)
            }
        }
    }
}
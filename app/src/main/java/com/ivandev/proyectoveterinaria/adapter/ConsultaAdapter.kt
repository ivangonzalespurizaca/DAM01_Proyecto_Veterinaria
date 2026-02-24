package com.ivandev.proyectoveterinaria.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemConsultaMedicaBinding
import com.ivandev.proyectoveterinaria.model.ConsultaMedica

class ConsultaAdapter(
    private val onItemClick: (ConsultaMedica) -> Unit
) : RecyclerView.Adapter<ConsultaAdapter.ConsultaViewHolder>() {

    private var listaConsultas: List<ConsultaMedica> = emptyList()
    private var listaFiltrada: List<ConsultaMedica> = emptyList()

    fun updateList(nuevaLista: List<ConsultaMedica>) {
        listaConsultas = nuevaLista
        listaFiltrada = nuevaLista
        notifyDataSetChanged()
    }

    fun filtrar(texto: String) {
        listaFiltrada = if (texto.isEmpty()) {
            listaConsultas // Si está vacío, volvemos a mostrar todo
        } else {
            // Filtramos siempre sobre la lista ORIGINAL
            listaConsultas.filter { consulta ->
                consulta.diagnostico.contains(texto, ignoreCase = true) ||
                        consulta.motivo.contains(texto, ignoreCase = true) ||
                        consulta.nombreMedicamento.contains(texto, ignoreCase = true) // Criterio extra
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultaViewHolder {
        val binding = ItemConsultaMedicaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConsultaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsultaViewHolder, position: Int) {
        holder.bind(listaFiltrada[position])
    }

    override fun getItemCount(): Int = listaFiltrada.size

    inner class ConsultaViewHolder(private val binding: ItemConsultaMedicaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(consulta: ConsultaMedica) {
            val context = binding.root.context
            binding.tvFechaConsulta.text = consulta.fechaConsulta
            binding.tvMotivoConsulta.text = consulta.motivo
            binding.tvSignosVitales.text = "Peso: ${consulta.pesoActual}kg | Temp: ${consulta.temperatura}°C"

            // Mostramos el badge solo si hay tratamiento
            if (consulta.nombreMedicamento.isNotEmpty()) {
                // --- CASO: CON TRATAMIENTO (Verde) ---
                binding.tvBadgeTratamiento.text = "CON TRATAMIENTO"
                binding.tvBadgeTratamiento.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.brand_green)
                )
            } else {
                // --- CASO: SIN TRATAMIENTO (Gris o Naranja suave) ---
                binding.tvBadgeTratamiento.text = "SIN TRATAMIENTO"
                // Te sugiero un color neutro o el 'text_muted' para no distraer tanto
                binding.tvBadgeTratamiento.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.text_muted)
                )
            }

            // Click en toda la tarjeta para ver detalles
            binding.root.setOnClickListener {
                onItemClick(consulta)
            }
        }
    }
}
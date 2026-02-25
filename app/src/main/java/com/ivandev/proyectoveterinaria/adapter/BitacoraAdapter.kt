package com.ivandev.proyectoveterinaria.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemConsultaBitacoraBinding
import com.ivandev.proyectoveterinaria.model.ConsultaMedica

class BitacoraAdapter(
    private val onConsultaClick: (ConsultaMedica) -> Unit
) : RecyclerView.Adapter<BitacoraAdapter.BitacoraViewHolder>() {

    private var listaOriginal = listOf<ConsultaMedica>()
    private var listaFiltrada = listOf<ConsultaMedica>()

    fun updateList(nuevaLista: List<ConsultaMedica>) {
        listaOriginal = nuevaLista
        listaFiltrada = nuevaLista
        notifyDataSetChanged()
    }

    fun filtrar(query: String) {
        val q = query.lowercase().trim()
        listaFiltrada = if (q.isEmpty()) {
            listaOriginal
        } else {
            listaOriginal.filter {
                it.nombreMascota.lowercase().contains(q) ||
                        it.fechaConsulta.contains(q) ||
                        it.diagnostico.lowercase().contains(q)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BitacoraViewHolder {
        val binding = ItemConsultaBitacoraBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BitacoraViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BitacoraViewHolder, position: Int) {
        holder.bind(listaFiltrada[position], onConsultaClick)
    }

    override fun getItemCount(): Int = listaFiltrada.size

    class BitacoraViewHolder(private val binding: ItemConsultaBitacoraBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(consulta: ConsultaMedica, onClick: (ConsultaMedica) -> Unit) {
            binding.apply {
                tvNombrePaciente.text = consulta.nombreMascota
                tvFechaBitacora.text = consulta.fechaConsulta
                tvMotivoBitacora.text = "Motivo: ${consulta.motivo}"
                tvDiagnosticoPrevio.text = "Diag: ${consulta.diagnostico}"

                // Lógica del Badge de Tratamiento
                if (consulta.nombreMedicamento.isNotEmpty()) {
                    tvBadgeTratamiento.text = "CON TRATAMIENTO"
                    tvBadgeTratamiento.backgroundTintList = ColorStateList.valueOf(
                        root.context.getColor(R.color.brand_green)
                    )
                } else {
                    tvBadgeTratamiento.text = "SOLO CONTROL"
                    tvBadgeTratamiento.backgroundTintList = ColorStateList.valueOf(
                        root.context.getColor(R.color.text_muted)
                    )
                }

                // Carga de foto de la mascota con Glide
                Glide.with(root.context)
                    .load(consulta.fotoMascota)
                    .placeholder(R.drawable.ic_pet)
                    .centerCrop()
                    .into(ivFotoPaciente)

                // Acción al tocar la tarjeta: Abre el Detalle
                root.setOnClickListener { onClick(consulta) }
            }
        }
    }
}
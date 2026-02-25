package com.ivandev.proyectoveterinaria.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemMascotaAdopcionBinding
import com.ivandev.proyectoveterinaria.model.MascotaAdopcion

class MascotasEnAdopcionAdapter(
    private var lista: MutableList<MascotaAdopcion>,
    private var especiesMap: Map<String, String> = emptyMap(),
    private var razasMap: Map<String, String> = emptyMap(),
    private val onEditar: (MascotaAdopcion) -> Unit
) : RecyclerView.Adapter<MascotasEnAdopcionAdapter.AdopcionViewHolder>() {

    class AdopcionViewHolder(val binding: ItemMascotaAdopcionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdopcionViewHolder {
        val binding = ItemMascotaAdopcionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AdopcionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdopcionViewHolder, position: Int) {
        val mascota = lista[position]
        val context = holder.binding.root.context

        holder.binding.apply {
            tvNombreAdopcion.text = mascota.nombreMascota

            // Lógica de traducción de IDs
            val nombreEspecie = especiesMap[mascota.idEspecie] ?: "Desconocida"
            val nombreRaza = razasMap[mascota.idRaza] ?: "Desconocida"
            tvRazaEspecieAdopcion.text = "$nombreRaza - $nombreEspecie"

            // Lógica de edad
            val sufijoEdad = if(mascota.edadEstimada == "1") "año" else "años"
            tvDetallesAdopcion.text = "${mascota.sexo} • ${mascota.edadEstimada} $sufijoEdad"

            // 1. Lógica de Colores según el Estado
            tvEstadoAdopcion.text = mascota.estado.uppercase()

            val colorRes = when (mascota.estado.lowercase()) {
                "disponible" -> R.color.brand_green  // Verde por ejemplo
                "en proceso" -> R.color.brand_orange   // Naranja por ejemplo
                "adoptado"   -> R.color.text_muted   // Gris o Azul
                else         -> R.color.black
            }
            tvEstadoAdopcion.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, colorRes)
            )

            // 2. Bloqueo de Edición si está "Aprobado"
            if (mascota.estado.lowercase() == "adoptado") {
                root.setOnClickListener(null)
                root.alpha = 0.6f
            } else {
                root.alpha = 1.0f
                root.setOnClickListener { onEditar(mascota) }
            }

            Glide.with(context)
                .load(mascota.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoAdopcion)
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizarLista(nuevaLista: List<MascotaAdopcion>){
        lista.clear()
        lista.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    fun actualizarMapas(nuevasEspecies: Map<String, String>, nuevasRazas: Map<String, String>) {
        this.especiesMap = nuevasEspecies
        this.razasMap = nuevasRazas
        notifyDataSetChanged() // Esto refresca las tarjetas con los nombres reales
    }
}
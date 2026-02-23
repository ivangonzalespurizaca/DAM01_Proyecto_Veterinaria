package com.ivandev.proyectoveterinaria.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
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
        holder.binding.apply {
            tvNombreAdopcion.text = mascota.nombreMascota

            val sufijoEdad = if(mascota.edadEstimada == "1") "año" else "años"
            // Traducimos los IDs a nombres usando los mapas
            val nombreEspecie = especiesMap[mascota.idEspecie] ?: "Desconocida"
            val nombreRaza = razasMap[mascota.idRaza] ?: "Desconocida"
            tvRazaEspecieAdopcion.text = "$nombreRaza - $nombreEspecie"

            tvDetallesAdopcion.text = "${mascota.sexo} • ${mascota.edadEstimada} $sufijoEdad"
            tvEstadoAdopcion.text = mascota.estado.uppercase()

            Glide.with(root.context)
                .load(mascota.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoAdopcion)

            root.setOnClickListener { onEditar(mascota) }
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
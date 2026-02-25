package com.ivandev.proyectoveterinaria.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.model.Historial
class HistorialAdapter(private var listaConsultas: List<Historial>) :
    RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreMascota: TextView = view.findViewById(R.id.tvTobyName)
        val tvVeteNombre: TextView = view.findViewById(R.id.tvVeteNombre)
        val tvHora: TextView = view.findViewById(R.id.tvHoraConsulta)
        val tvMotivo: TextView = view.findViewById(R.id.tvMotivo)
        val tvFecha: TextView = view.findViewById(R.id.tvFechaCorta)
        val viewAccent: View = view.findViewById(R.id.viewAccent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val item = listaConsultas[position]

        // 1. Mostrar Textos
        holder.tvNombreMascota.text = if (item.especieNombre.isNotEmpty()) {
            "${item.nombreMascota} (${item.especieNombre})"
        } else {
            item.nombreMascota
        }

        holder.tvVeteNombre.text = "Veterinario: ${item.nombreVeterinario}"
        holder.tvHora.text = item.fechaConsulta
        holder.tvMotivo.text = item.motivo


        val colorEspecie = when (item.especieNombre.lowercase()) {
            "canino", "perro" -> "#7B42F6" // Morado original
            "felino", "gato"  -> "#42A5F5" // Azul para gatos
            "ave", "pájaro"   -> "#FB8C00" // Naranja para aves
            "conejo"          -> "#F06292" // Rosa para conejos
            else              -> "#9E9E9E" // Gris para desconocidos
        }

        holder.viewAccent.setBackgroundColor(android.graphics.Color.parseColor(colorEspecie))

        // Opcional: También puedes cambiar el color del fondo del texto del motivo para que combine
        holder.tvMotivo.backgroundTintList = android.content.res.ColorStateList.valueOf(
            android.graphics.Color.parseColor(colorEspecie)
        ).withAlpha(40) // 40 es la opacidad para que se vea pastel
    }

    override fun getItemCount(): Int = listaConsultas.size

    fun updateList(newList: List<Historial>) {
        listaConsultas = newList
        notifyDataSetChanged()
    }
}
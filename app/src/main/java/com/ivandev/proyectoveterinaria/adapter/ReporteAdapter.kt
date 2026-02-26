package com.ivandev.proyectoveterinaria.adapter


import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import com.ivandev.proyectoveterinaria.model.ReporteConsulta
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ivandev.proyectoveterinaria.R

class ReporteAdapter(
    private var lista: List<ReporteConsulta>,
    private val onClick: (ReporteConsulta) -> Unit // Función que recibe el clic
) : RecyclerView.Adapter<ReporteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombreMascota)
        val motivo: TextView = view.findViewById(R.id.txtMotivo)
        val fecha: TextView = view.findViewById(R.id.txtFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_consulta, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val consulta = lista[position]
        holder.nombre.text = consulta.nombreMascota
        holder.motivo.text = "Motivo: ${consulta.motivo}"
        holder.fecha.text = consulta.fechaConsulta

        holder.itemView.setOnClickListener { onClick(consulta) }
    }

    override fun getItemCount() = lista.size

    // Método para actualizar la lista cuando venga de Firebase
    fun updateData(newList: List<ReporteConsulta>) {
        lista = newList
        notifyDataSetChanged()
    }
}
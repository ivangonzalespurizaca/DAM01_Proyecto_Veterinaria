package com.ivandev.proyectoveterinaria.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.model.MascotaIntro

class MascotaClienteAdapter(private val listaMascotas: List<MascotaIntro>) :
    RecyclerView.Adapter<MascotaClienteAdapter.MascotaViewHolder>() {

    class MascotaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.txtNombreMascotaItem)
        val raza: TextView = view.findViewById(R.id.txtRazaMascotaItem)
        val imagen: ImageView = view.findViewById(R.id.imgMascotaItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mascota_horizontal, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = listaMascotas[position]
        holder.nombre.text = mascota.nombreMascota
        holder.raza.text = "${mascota.nombreEspecie} - ${mascota.nombreRaza}"

        // 1. Extraemos la URL de la mascota actual (ajusta 'foto' si en tu modelo se llama distinto)
        val fotoUrl = mascota.foto ?: ""

        // 2. Ahora sí usamos Glide con la variable definida
        Glide.with(holder.itemView.context)
            .load(fotoUrl.ifBlank { R.drawable.load }) // ifBlank es más seguro para Strings
            .placeholder(R.drawable.load)
            .error(R.drawable.load)
            .circleCrop()
            .into(holder.imagen)
    }

    override fun getItemCount(): Int = listaMascotas.size
}
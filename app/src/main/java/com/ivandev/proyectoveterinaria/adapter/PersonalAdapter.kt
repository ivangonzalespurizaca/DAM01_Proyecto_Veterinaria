package com.ivandev.proyectoveterinaria.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.ItemPersonalCardBinding
import com.ivandev.proyectoveterinaria.model.Usuario
import com.ivandev.proyectoveterinaria.model.VeterinarioCompleto

class PersonalAdapter(private var listaPersonal: MutableList<VeterinarioCompleto>,
    private val onAnularCuenta: (Usuario) -> Unit
) : RecyclerView.Adapter<PersonalAdapter.PersonalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonalViewHolder {
        val binding = ItemPersonalCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PersonalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonalViewHolder, position: Int) {
        holder.bind(listaPersonal[position])
    }

    override fun getItemCount(): Int = listaPersonal.size

    fun actualizarLista(nuevaLista: List<VeterinarioCompleto>) {
        listaPersonal = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    inner class PersonalViewHolder(val binding: ItemPersonalCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(vet: VeterinarioCompleto) {
            val usuario = vet.usuario
            binding.tvNombreProfesional.text = usuario.nombreCompleto
            binding.tvCmvpProfesional.text = vet.colegiatura ?: "Sin Colegiatura"
            binding.tvDniProfesional.text = "DNI: ${usuario.dni}"
            if (!usuario.foto.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(usuario.foto)
                    .circleCrop()
                    .placeholder(R.drawable.ic_perfil_usuario)
                    .error(R.drawable.ic_perfil_usuario)
                    .into(binding.ivFotoProfesional)
            } else {
                binding.ivFotoProfesional.setImageResource(R.drawable.ic_perfil_usuario)
            }

            if (usuario.estado == "Inactivo") {
                binding.root.alpha = 0.5f
            } else {
                binding.root.alpha = 1.0f
            }

            binding.btnMenuOpciones.setOnClickListener { view ->
                mostrarMenuContextual(view, usuario)
            }
        }

        private fun mostrarMenuContextual(view: View, usuario: Usuario) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_item_personal, popup.menu)

            val itemBaja = popup.menu.findItem(R.id.item_dar_baja)

            if (usuario.estado == "Inactivo") {
                itemBaja.title = "Reactivar Cuenta"
            } else {
                itemBaja.title = "Dar de baja"
            }

            popup.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.item_dar_baja) {
                    onAnularCuenta(usuario)
                    true
                } else false
            }
            popup.show()
        }
    }
}
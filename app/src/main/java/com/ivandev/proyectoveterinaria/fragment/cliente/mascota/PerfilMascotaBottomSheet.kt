package com.ivandev.proyectoveterinaria.fragment.cliente.mascota

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.databinding.BottomSheetPerfilMascotaBinding
import com.ivandev.proyectoveterinaria.model.Mascota
import com.ivandev.proyectoveterinaria.utils.QRHelper

class PerfilMascotaBottomSheet(
    private val mascota: Mascota,
    private val nombreRaza: String,
    private val nombreEspecie: String
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPerfilMascotaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetPerfilMascotaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            tvNombrePerfil.text = mascota.nombreMascota
            tvResumenPerfil.text = "${nombreRaza.uppercase()} . ${nombreEspecie.uppercase()} . ${mascota.sexo.uppercase()}"
            tvFechaNacPerfil.text = mascota.fechaNacimiento
            tvPesoPerfil.text = "${mascota.pesoInicial} kg"
            tvCodigoPerfil.text = "REF-${mascota.idMascota.takeLast(6).uppercase()}"

            // Cargamos la foto con Glide
            Glide.with(requireContext())
                .load(mascota.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoPerfilMascota)

            // Generamos el QR usando nuestro Helper
            val qrBitmap = QRHelper.generarQR(mascota.idMascota, 500)
            ivQrPerfil.setImageBitmap(qrBitmap)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
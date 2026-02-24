package com.ivandev.proyectoveterinaria.fragment.veterinario.consultas

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentAccionesConsultaBinding
import com.ivandev.proyectoveterinaria.databinding.FragmentHistorialVacunasBinding
import com.ivandev.proyectoveterinaria.fragment.admin.catalogo.RazaListadoFragment
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Mascota

class AccionesConsultaFragment : Fragment(R.layout.fragment_acciones_consulta), IFragmentoToolbar {
    override val titulo: String = "GESTIONAR CONSULTAS"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO
    private var _binding: FragmentAccionesConsultaBinding? = null
    private val binding get() = _binding!!
    private var mascota: Mascota? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccionesConsultaBinding.bind(view)

        // 1. RECUPERAR EL OBJETO: Aquí es donde "jalamos" los datos
        mascota = arguments?.let{
            BundleCompat.getParcelable(it, "mascota", Mascota::class.java)
        }
        // 2. LOG DE SEGURIDAD: Para ver en Logcat si llegó bien
        println("DEBUG: Mascota recibida -> ${mascota?.nombreMascota}")

        if (mascota != null) {
            setupUI(mascota!!)
            setupButtons()

            binding.cardMascotaSeleccionada.root.setOnClickListener {
                mostrarDetallesMascota(mascota!!)
            }

        } else {
            Toast.makeText(context, "Error: No se cargaron los datos del paciente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI(m: Mascota) {
        binding.cardMascotaSeleccionada.apply {
            tvNombreBusqueda.text = m.nombreMascota
            tvRazaBusqueda.text = "Identificador: ${m.idMascota.takeLast(6).uppercase()}"

            Glide.with(requireContext())
                .load(m.foto)
                .placeholder(R.drawable.ic_pet)
                .circleCrop()
                .into(ivFotoBusqueda)
        }
    }

    private fun mostrarDetallesMascota(mascota: Mascota) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_detalle_mascota, null)

        // Referencias y llenado de datos
        dialogView.findViewById<TextView>(R.id.tvNombreMascota).text = mascota.nombreMascota
        dialogView.findViewById<TextView>(R.id.tvRazaEspecie).text = "${mascota.nombreEspecie} - ${mascota.nombreRaza}"

        // Nuevos campos del dueño
        dialogView.findViewById<TextView>(R.id.tvNombreDueno).text = "Propietario: ${mascota.nombreDueno}"
        dialogView.findViewById<TextView>(R.id.tvDniDueno).text = "DNI: ${mascota.dniDueno}"

        dialogView.findViewById<TextView>(R.id.tvEdad).text = "${mascota.fechaNacimiento}"
        dialogView.findViewById<TextView>(R.id.tvSexo).text = mascota.sexo
        dialogView.findViewById<TextView>(R.id.tvPesoInicial).text = "${mascota.pesoInicial} kg"

        // Carga de imagen con Glide
        val ivFoto = dialogView.findViewById<ImageView>(R.id.ivDetalleMascota)
        Glide.with(this).load(mascota.foto).placeholder(R.drawable.ic_pet).circleCrop().into(ivFoto)

        builder.setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun setupButtons() {
        binding.btnNuevaVacuna.setOnClickListener {
            val fragmentNuevaVacuna = RegistrarAplicacionVacunaFragment()

            val bundle = Bundle().apply {
                putParcelable("mascota", mascota)
            }

            fragmentNuevaVacuna.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragmentNuevaVacuna)
                .addToBackStack(null)
                .commit()
        }

        binding.btnCarnetDeVacunas.setOnClickListener {
            val fragmentHistorialVacunas = HistorialVacunasFragment()

            val bundle = Bundle().apply {
                putParcelable("mascota", mascota)
            }

            fragmentHistorialVacunas.arguments = bundle


            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragmentHistorialVacunas)
                .addToBackStack(null)
                .commit()
        }

        binding.btnNuevaConsulta.setOnClickListener {
            val fragmento = RegistrarConsultaFragment()

            fragmento.arguments = Bundle().apply { putParcelable("mascota", mascota) }

            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragmento)
                .addToBackStack(null)
                .commit()
        }

        binding.btnHistorialMedico.setOnClickListener {
            val fragmentHistorialMedico = HistorialMedicoFragment()

            val bundle = Bundle().apply{
                putParcelable("mascota", mascota)
            }

            fragmentHistorialMedico.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragmentHistorialMedico)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
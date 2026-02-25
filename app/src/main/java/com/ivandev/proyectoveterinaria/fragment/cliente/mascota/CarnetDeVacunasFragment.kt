package com.ivandev.proyectoveterinaria.fragment.cliente.mascota

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.BundleCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.VacunaMiMascotaAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentCarnetDeVacunasBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Mascota
import com.ivandev.proyectoveterinaria.model.VacunaAplicada
import com.ivandev.proyectoveterinaria.viewmodel.VacunaAplicadaViewModel

class CarnetDeVacunasFragment : Fragment(R.layout.fragment_carnet_de_vacunas), IFragmentoToolbar {

    override val titulo: String = "CARNET DE VACUNACIÓN"
    override val tipo = PanelPrincipalActivity.TipoToolbar.SECUNDARIO

    private var _binding: FragmentCarnetDeVacunasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VacunaAplicadaViewModel by viewModels()
    private lateinit var vacunaAdapter: VacunaMiMascotaAdapter
    private var mascota: Mascota? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCarnetDeVacunasBinding.bind(view)

        // 1. Recuperamos el objeto Mascota completo del bundle
        mascota = arguments?.let {
            BundleCompat.getParcelable(it, "mascota", Mascota::class.java)
        }

        setupRecyclerView()
        setupSearch()
        setupObservers()

        // 2. Iniciamos la carga de datos usando el ID de la mascota
        mascota?.idMascota?.let { id ->
            viewModel.listarVacunasPorMascota(id)
        }
    }

    private fun setupRecyclerView() {
        vacunaAdapter = VacunaMiMascotaAdapter { vacuna ->
            mostrarDetallesDialog(vacuna)
        }

        binding.rvVacunas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = vacunaAdapter
        }
    }

    private fun setupObservers() {
        // Observamos el LiveData que ya viene ordenado desde tu ViewModel
        viewModel.listaHistorial.observe(viewLifecycleOwner) { lista ->
            vacunaAdapter.updateList(lista)
        }
    }

    private fun setupSearch() {
        binding.etBuscarVacuna.addTextChangedListener { text ->
            vacunaAdapter.filtrar(text.toString())
        }
    }

    private fun mostrarDetallesDialog(vacuna: VacunaAplicada) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_detalle_vacuna, null)

        // Referencias de las vistas del layout personalizado
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvDetalleTitulo)
        val tvDosis = dialogView.findViewById<TextView>(R.id.tvDetalleDosis)
        val tvFecha = dialogView.findViewById<TextView>(R.id.tvDetalleFecha)
        val tvObs = dialogView.findViewById<TextView>(R.id.tvDetalleObservaciones)
        val ivEstado = dialogView.findViewById<ImageView>(R.id.ivDetalleEstadoIcono)

        // Seteo de datos
        tvTitulo.text = vacuna.nombreVacuna
        tvDosis.text = "Dosis número: ${vacuna.nroDosis}"
        tvObs.text = if (vacuna.observaciones.isNullOrEmpty()) "Sin notas adicionales." else vacuna.observaciones

        // Lógica visual por estado
        if (vacuna.estado == "Pendiente") {
            tvFecha.text = "Programada para: ${vacuna.fechaProgramada}"
            ivEstado.setImageResource(R.drawable.ic_clock)
            ivEstado.imageTintList = ColorStateList.valueOf(requireContext().getColor(R.color.brand_orange))
            tvTitulo.setTextColor(requireContext().getColor(R.color.brand_orange))
        } else {
            tvFecha.text = "Aplicada el: ${vacuna.fechaAplicacion}"
            ivEstado.setImageResource(R.drawable.ic_vaccine)
            ivEstado.imageTintList = ColorStateList.valueOf(requireContext().getColor(R.color.brand_green))
            tvTitulo.setTextColor(requireContext().getColor(R.color.brand_green))
        }

        builder.setView(dialogView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
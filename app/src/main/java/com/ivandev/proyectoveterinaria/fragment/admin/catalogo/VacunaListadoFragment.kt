package com.ivandev.proyectoveterinaria.fragment.admin.catalogo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.VacunaAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentVacunaListadoBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Vacuna
import com.ivandev.proyectoveterinaria.viewmodel.VacunasViewModel

class VacunasListadoFragment : Fragment(R.layout.fragment_vacuna_listado), IFragmentoToolbar {

    override val titulo: String = "CATÁLOGO DE VACUNAS"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO

    private var _binding: FragmentVacunaListadoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VacunasViewModel by viewModels()
    private lateinit var adapterVacuna: VacunaAdapter
    private var listaOriginal = listOf<Vacuna>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVacunaListadoBinding.bind(view)

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        binding.fabAgregarVacuna.setOnClickListener {
            abrirEditorVacuna()
        }

        viewModel.cargarVacunas()
    }

    private fun setupRecyclerView() {
        adapterVacuna = VacunaAdapter(mutableListOf()) { vacuna ->
            abrirEditorVacuna(vacuna)
        }
        binding.rvVacunas.adapter = adapterVacuna
        binding.rvVacunas.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSearch() {
        binding.etBuscarVacuna.addTextChangedListener { text ->
            val query = text.toString().lowercase()
            val filtrada = listaOriginal.filter { it.nombreVacuna.lowercase().contains(query) }
            adapterVacuna.actualizarLista(filtrada)
        }
    }

    private fun observeViewModel() {
        viewModel.listaVacunas.observe(viewLifecycleOwner) { lista ->
            listaOriginal = lista
            adapterVacuna.actualizarLista(lista)
        }
    }

    private fun abrirEditorVacuna(vacuna: Vacuna? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_vacuna, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Referencias de los componentes del diálogo
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloVacunaDialog)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreVacuna)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarVacuna)
        val btnEliminar = dialogView.findViewById<Button>(R.id.btnEliminarVacuna)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.btnCancelarVacuna)

        if (vacuna != null) {
            // Modo Edición
            tvTitulo.text = "EDITAR VACUNA"
            btnGuardar.text = "ACTUALIZAR"
            btnEliminar.visibility = View.VISIBLE
            etNombre.setText(vacuna.nombreVacuna)

            btnEliminar.setOnClickListener {
                dialog.dismiss()
                confirmarEliminacion(vacuna)
            }
        } else {
            // Modo Registro
            tvTitulo.text = "NUEVA VACUNA"
            btnGuardar.text = "REGISTRAR"
            btnEliminar.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                etNombre.error = "Campo obligatorio"
                return@setOnClickListener
            }

            val vacunaData = Vacuna(idVacuna = vacuna?.idVacuna ?: "", nombreVacuna = nombre)
            viewModel.guardarVacuna(vacunaData) { exito ->
                if (exito) {
                    Toast.makeText(requireContext(), "Vacuna guardada", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun confirmarEliminacion(vacuna: Vacuna) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Eliminar ${vacuna.nombreVacuna}?")
            .setMessage("Se verificará si la vacuna ya ha sido aplicada a alguna mascota.")
            .setPositiveButton("ELIMINAR") { _, _ ->
                viewModel.eliminarVacuna(vacuna.idVacuna) { exito, error ->
                    if (exito) {
                        Toast.makeText(context, "Vacuna eliminada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, error ?: "Error al eliminar", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
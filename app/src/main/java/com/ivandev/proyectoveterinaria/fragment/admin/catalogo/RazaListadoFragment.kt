package com.ivandev.proyectoveterinaria.fragment.admin.catalogo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.adapter.RazaAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentRazaListadoBinding
import com.ivandev.proyectoveterinaria.model.Especie
import com.ivandev.proyectoveterinaria.viewmodel.EspeciesViewModel
import com.ivandev.proyectoveterinaria.viewmodel.RazasViewModel
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Raza
import com.ivandev.proyectoveterinaria.room.DBHelper

class RazaListadoFragment : Fragment(R.layout.fragment_raza_listado), IFragmentoToolbar {
    override val titulo: String = "RAZAS"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO
    private var _binding: FragmentRazaListadoBinding? = null
    private val binding get() = _binding!!
    private val viewModelRazas: RazasViewModel by viewModels()
    private lateinit var dbHelper: DBHelper
    private val viewModelEspecies: EspeciesViewModel by viewModels()
    private var listaOriginal = listOf<Raza>()

    private lateinit var adapterRaza: RazaAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dbHelper = DBHelper.getInstance(requireContext())
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DBHelper.getInstance(requireContext())
        _binding = FragmentRazaListadoBinding.bind(view)

        setupRecyclerView()
        observeData()
        setupSearch()

        // Listener para el botón flotante de agregar
        binding.fabAgregarRaza.setOnClickListener {
            abrirEditorRaza()
        }

        viewModelEspecies.cargarEspecies(dbHelper)
        viewModelRazas.cargarRazas(dbHelper)
    }

    private fun observeData() {
        viewModelEspecies.listaEspecies.observe(viewLifecycleOwner) { especies ->
            val mapa = especies.associate { it.id to it.nombre }
            adapterRaza.actualizarMapaEspecies(mapa)
        }

        viewModelRazas.listaRazas.observe(viewLifecycleOwner) { razas ->
            listaOriginal = razas
            adapterRaza.actualizarLista(razas)
        }
    }

    private fun setupRecyclerView() {
        adapterRaza = RazaAdapter(mutableListOf()) { raza ->
            abrirEditorRaza(raza)
        }
        binding.rvRazas.adapter = adapterRaza
        binding.rvRazas.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSearch() {
        binding.etBuscarRaza.addTextChangedListener { text ->
            val query = text.toString().lowercase()
            val filtrada = listaOriginal.filter { it.nombre.lowercase().contains(query) }
            adapterRaza.actualizarLista(filtrada)
        }
    }

    private fun abrirEditorRaza(raza: Raza? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_raza, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloDialog)
        val actvEspecie = dialogView.findViewById<AutoCompleteTextView>(R.id.actvEspecie)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreRaza)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarEdicion)
        val btnEliminar = dialogView.findViewById<Button>(R.id.btnEliminar)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.btnCancelarEdicion)

        // Configuración del Selector de Especies (Combo Box)
        var idEspecieSeleccionada = ""
        val especies = viewModelEspecies.listaEspecies.value ?: emptyList()
        val nombresEspecies = especies.map { it.nombre }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombresEspecies)

        actvEspecie.setAdapter(spinnerAdapter)
        actvEspecie.setOnItemClickListener { _, _, position, _ ->
            idEspecieSeleccionada = especies[position].id
        }

        if (raza != null) {
            tvTitulo.text = "EDITAR RAZA"
            etNombre.setText(raza.nombre)
            btnEliminar.visibility = View.VISIBLE

            // Pre-seleccionar la especie actual
            val especieActual = especies.find { it.id == raza.idEspecie }
            actvEspecie.setText(especieActual?.nombre, false)
            idEspecieSeleccionada = raza.idEspecie

            btnEliminar.setOnClickListener {
                dialog.dismiss()
                confirmarEliminacion(raza)
            }
        } else {
            tvTitulo.text = "NUEVA RAZA"
            btnEliminar.visibility = View.GONE
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()

            if (nombre.isEmpty() || idEspecieSeleccionada.isEmpty()) {
                Toast.makeText(requireContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val razaData = Raza(
                id = raza?.id ?: "",
                nombre = nombre,
                idEspecie = idEspecieSeleccionada
            )

            viewModelRazas.guardarRaza(razaData, dbHelper) { exito ->
                if (exito) {
                    Toast.makeText(requireContext(), "Raza guardada", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun confirmarEliminacion(raza: Raza) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Eliminar ${raza.nombre}?")
            .setMessage("Esta acción verificará si existen mascotas registradas con esta raza.")
            .setPositiveButton("ELIMINAR") { _, _ ->
                viewModelRazas.eliminarRaza(raza.id, dbHelper) { exito, error ->
                    if (exito) {
                        Toast.makeText(context, "Raza eliminada", Toast.LENGTH_SHORT).show()
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
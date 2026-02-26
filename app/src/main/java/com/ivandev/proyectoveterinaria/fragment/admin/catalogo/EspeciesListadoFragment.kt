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
import com.ivandev.proyectoveterinaria.adapter.EspecieAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentEspeciesListadoBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Especie
import com.ivandev.proyectoveterinaria.room.DBHelper
import com.ivandev.proyectoveterinaria.viewmodel.EspeciesViewModel

class EspeciesListadoFragment : Fragment(R.layout.fragment_especies_listado), IFragmentoToolbar {
    override val titulo: String = "ESPECIES"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.SECUNDARIO
    private var _binding: FragmentEspeciesListadoBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHelper
    private val viewModel: EspeciesViewModel by viewModels()
    private lateinit var adapterEspecie: EspecieAdapter
    private var listaOriginal = listOf<Especie>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dbHelper = DBHelper.getInstance(requireContext())
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEspeciesListadoBinding.bind(view)

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        // IMPORTANTE: Configurar el FAB para agregar nuevas especies
        binding.fabAgregarEspecie.setOnClickListener {
            abrirEditorEspecie()
        }

        viewModel.cargarEspecies(dbHelper)
    }

    private fun setupRecyclerView() {
        adapterEspecie = EspecieAdapter(mutableListOf()) { especie ->
            abrirEditorEspecie(especie)
        }
        binding.rvEspecies.adapter = adapterEspecie
        binding.rvEspecies.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun confirmarEliminacion(especie: Especie) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Eliminar ${especie.nombre}?")
            .setMessage("Esta acción verificará si existen razas asociadas.")
            .setPositiveButton("ELIMINAR") { _, _ ->
                viewModel.eliminarEspecie(especie.id, dbHelper) { exito, error ->
                    if (exito) {
                        Toast.makeText(context, "Especie eliminada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, error ?: "Error desconocido", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun setupSearch() {
        binding.etBuscarEspecie.addTextChangedListener { text ->
            val query = text.toString().lowercase()
            val filtrada = listaOriginal.filter { it.nombre.lowercase().contains(query) }
            adapterEspecie.actualizarLista(filtrada)
        }
    }

    private fun observeViewModel() {
        viewModel.listaEspecies.observe(viewLifecycleOwner) { lista ->
            listaOriginal = lista
            adapterEspecie.actualizarLista(lista)
        }
    }

    private fun abrirEditorEspecie(especie: Especie? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_especie, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Referencias de los componentes
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloDialog)
        val tvInstrucciones = dialogView.findViewById<TextView>(R.id.tvInstrucciones)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreEspecie)
        val etDefinicion = dialogView.findViewById<EditText>(R.id.etDefinicionEspecie)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarEdicion)
        val btnEliminar = dialogView.findViewById<Button>(R.id.btnEliminar)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.btnCancelarEdicion)

        // Lógica de Adaptación de Interfaz
        if (especie != null) {
            // MODO EDICIÓN
            tvTitulo.text = "EDITAR ESPECIE"
            tvInstrucciones.text = "Modifica los campos necesarios para actualizar la información."
            btnGuardar.text = "ACTUALIZAR"
            btnEliminar.visibility = View.VISIBLE // El botón es visible aquí

            etNombre.setText(especie.nombre)
            etDefinicion.setText(especie.definicion)

            // Configuración del botón eliminar dentro del editor
            btnEliminar.setOnClickListener {
                dialog.dismiss() // Cerramos el editor antes de confirmar
                confirmarEliminacion(especie)
            }
        } else {
            // MODO REGISTRO
            tvTitulo.text = "NUEVA ESPECIE"
            tvInstrucciones.text = "Ingresa el nombre y definición para registrar una nueva especie."
            btnGuardar.text = "REGISTRAR"
            btnEliminar.visibility = View.GONE // OCULTAMOS el botón innecesario
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val definicion = etDefinicion.text.toString().trim()

            if (nombre.isEmpty()) {
                etNombre.error = "Campo obligatorio"
                return@setOnClickListener
            }

            val especieData = Especie(
                id = especie?.id ?: "",
                nombre = nombre,
                definicion = definicion
            )

            viewModel.guardarEspecie(especieData, dbHelper) { exito ->
                if (exito) {
                    Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
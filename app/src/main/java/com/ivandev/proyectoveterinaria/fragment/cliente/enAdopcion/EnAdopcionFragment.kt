package com.ivandev.proyectoveterinaria.fragment.cliente.enAdopcion

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.adapter.MascotaAdopcionClienteAdapter
import com.ivandev.proyectoveterinaria.databinding.FragmentEnAdopcionBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.MascotaAdopcion
import com.ivandev.proyectoveterinaria.viewmodel.MascotaAdopcionViewModel

class EnAdopcionFragment : Fragment(R.layout.fragment_en_adopcion), IFragmentoToolbar {
    override val titulo: String = "EN ADOPCIÓN"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentEnAdopcionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MascotaAdopcionViewModel by viewModels()
    private lateinit var adapter: MascotaAdopcionClienteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEnAdopcionBinding.bind(view)

        setupRecyclerView()
        setupSearch()

        viewModel.cargarMascotasParaCliente()

        viewModel.listaMascotas.observe(viewLifecycleOwner) { lista ->
            adapter.actualizarLista(lista)
        }

        binding.btnVerSolicitudes.setOnClickListener {
            // 1. Instanciamos el nuevo fragmento
            val fragmentoDestino = MisSolicitudesFragment()

            // 2. Realizamos la transacción
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,       // Opcional: para el regreso
                    R.anim.slide_out_left  // Opcional: para el regreso
                )
                .replace(R.id.nav_host_fragment, fragmentoDestino) // Asegúrate que el ID sea el de tu FrameLayout
                .addToBackStack(null) // Esto permite que el cliente regrese al presionar "Atrás"
                .commit()
        }
    }

    private fun setupRecyclerView() {
        adapter = MascotaAdopcionClienteAdapter(
            listaMascotas = emptyList(),
            onDetalles = { mascota -> mostrarDialogoDetalles(mascota) },
            onSolicitar = { mascota -> confirmarSolicitud(mascota) }
        )

        binding.rvAdopcionCliente.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvAdopcionCliente.adapter = adapter //
    }

    private fun setupSearch() {
        // Filtro en tiempo real
        binding.etBuscarMascotaCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                val listaFiltrada = viewModel.filtrarMascotas(query)
                adapter.actualizarLista(listaFiltrada)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun mostrarDialogoDetalles(mascota: MascotaAdopcion) {
        val builder = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_detalle_mascota_adopcion, null)

        // 1. Referencias de las vistas
        val ivFoto = view.findViewById<ImageView>(R.id.ivDetalleFoto)
        val tvNombre = view.findViewById<TextView>(R.id.tvDetalleNombre)
        val tvRazaEspecie = view.findViewById<TextView>(R.id.tvDetalleRazaEspecie)
        val tvStats = view.findViewById<TextView>(R.id.tvDetalleStats)
        val tvDescripcion = view.findViewById<TextView>(R.id.tvDetalleDescripcion)
        val tvContacto = view.findViewById<TextView>(R.id.tvDetalleContacto)

        // 2. Llenado de datos (Desnormalizados)
        tvNombre.text = mascota.nombreMascota
        tvRazaEspecie.text = "${mascota.nombreRaza} - ${mascota.nombreEspecie}"
        tvStats.text = "Edad: ${mascota.edadEstimada} | Sexo: ${mascota.sexo}"
        tvDescripcion.text = mascota.descripcion
        tvContacto.text = "Teléfono: ${mascota.contacto}"

        // 3. Carga de imagen con Glide
        Glide.with(requireContext())
            .load(mascota.foto)
            .placeholder(R.drawable.ic_pet)
            .circleCrop()
            .into(ivFoto)

        // 4. Configuración del Diálogo
        builder.setView(view)
            .setPositiveButton("ME INTERESA") { _, _ -> confirmarSolicitud(mascota) }
            .setNegativeButton("CERRAR", null)
            .show()
    }

    private fun confirmarSolicitud(mascota: MascotaAdopcion) {
        // 1. Creamos un diálogo de "Cargando" opcional pero recomendado
        val cargando = AlertDialog.Builder(requireContext())
            .setMessage("Verificando estado...")
            .setCancelable(false)
            .show()

        viewModel.verificarSiYaPostulo(mascota.idMascotaAdopcion) { yaPostulo ->
            cargando.dismiss() // Quitamos el aviso de carga

            if (yaPostulo) {
                // Caso: Ya existe solicitud
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Solicitud ya enviada")
                    .setMessage("Ya has enviado una postulación para ${mascota.nombreMascota}. Puedes ver el estado en la sección 'Mis Solicitudes'.")
                    .setPositiveButton("ENTENDIDO", null)
                    .show()
            } else {
                // Caso: Puede postularse
                mostrarDialogoConfirmacion(mascota)
            }
        }
    }

    private fun mostrarDialogoConfirmacion(mascota: MascotaAdopcion) {
        val mensajeCuerpo = if (mascota.estado == "En Proceso") {
            "Esta mascota ya tiene un proceso iniciado, pero puedes enviar tu solicitud como candidato de respaldo."
        } else {
            "¿Deseas enviar una solicitud para adoptar a ${mascota.nombreMascota}?"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Postular para adopción")
            .setMessage(mensajeCuerpo)
            .setPositiveButton("ENVIAR SOLICITUD") { _, _ ->
                enviarSolicitudFinal(mascota)
            }
            .setNegativeButton("CANCELAR", null)
            .show()
    }

    private fun enviarSolicitudFinal(mascota: MascotaAdopcion) {
        viewModel.generarSolicitudAutomatica(mascota) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "¡Solicitud enviada con éxito!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
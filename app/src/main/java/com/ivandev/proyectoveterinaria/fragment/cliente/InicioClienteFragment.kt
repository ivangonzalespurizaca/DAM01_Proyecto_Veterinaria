package com.ivandev.proyectoveterinaria.fragment.cliente

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentInicioClienteBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar
import com.ivandev.proyectoveterinaria.model.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InicioClienteFragment : Fragment(R.layout.fragment_inicio_cliente), IFragmentoToolbar {
    override val titulo: String = "PANEL PRINCIPAL"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL
    private var _binding: FragmentInicioClienteBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInicioClienteBinding.bind(view)

        cargarDatosDeUsuario()
    }

    private fun cargarDatosDeUsuario() {
        binding.tvInicioCliente.text = "Cargando datos..."

        RetrofitClient.instance.getUsuarios().enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                val currentBinding = _binding ?: return

                if (response.isSuccessful) {
                    val users = response.body()
                    if (!users.isNullOrEmpty()) {
                        val builder = StringBuilder()
                        for (user in users) {
                            builder.append("Nombre: ${user.nombreCompleto}\n")
                            builder.append("Email: ${user.correo}\n\n")
                        }
                        currentBinding.tvInicioCliente.text = builder.toString()
                    } else {
                        currentBinding.tvInicioCliente.text = "No se encontraron usuarios."
                    }
                } else {
                    currentBinding.tvInicioCliente.text = "Error del servidor: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                binding.tvInicioCliente.text = "Error de conexión: ${t.message}"
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
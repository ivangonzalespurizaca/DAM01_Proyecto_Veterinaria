package com.ivandev.proyectoveterinaria.fragment.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivandev.proyectoveterinaria.R
import com.ivandev.proyectoveterinaria.activity.PanelPrincipalActivity
import com.ivandev.proyectoveterinaria.databinding.FragmentInicioAdminBinding
import com.ivandev.proyectoveterinaria.databinding.FragmentReportesBinding
import com.ivandev.proyectoveterinaria.interfaces.IFragmentoToolbar

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.properties.UnitValue
import com.ivandev.proyectoveterinaria.adapter.ReporteAdapter
import com.ivandev.proyectoveterinaria.model.ReporteConsulta
import java.io.File
import java.io.FileOutputStream

class ReportesFragment : Fragment(R.layout.fragment_reportes), IFragmentoToolbar {
    override val titulo: String = "REPORTES"
    override val tipo: PanelPrincipalActivity.TipoToolbar = PanelPrincipalActivity.TipoToolbar.PRINCIPAL

    private var _binding: FragmentReportesBinding? = null
    private val binding get() = _binding!!

    private lateinit var reporteAdapter: ReporteAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReportesBinding.bind(view)

        setupRecyclerView()
        cargarDatosDesdeFirebase()
    }

    private fun setupRecyclerView() {
        // Inicializamos el adapter con una lista vacía y la lógica del click
        reporteAdapter = ReporteAdapter(emptyList()) { consulta ->
            generarYAbrirPdf(consulta)
        }
        binding.rvReportes.apply { // Asegúrate que el ID en tu XML sea rvReportes
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reporteAdapter
        }
    }

    private fun cargarDatosDesdeFirebase() {
        db.collection("consultas_medicas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val lista = snapshot.toObjects(ReporteConsulta::class.java)
                    reporteAdapter.updateData(lista)
                }
            }
    }

    private fun generarYAbrirPdf(consulta: ReporteConsulta) {
        try {
            val nombreArchivo = "Reporte_${consulta.nombreMascota?.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(requireContext().getExternalFilesDir(null), nombreArchivo)
            val writer = PdfWriter(FileOutputStream(file))
            val pdf = PdfDocument(writer)
            val document = Document(pdf)

            // --- ENCABEZADO ---
            document.add(Paragraph("CLÍNICA VETERINARIA").setBold().setFontSize(20f).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
            document.add(Paragraph("Reporte de Consulta Médica").setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
            document.add(Paragraph("-----------------------------------------------------------------------"))

            // --- SECCIÓN 1: INFORMACIÓN GENERAL ---
            document.add(Paragraph("DATOS DE LA MASCOTA Y VETERINARIO").setBold().setUnderline())
            val tablaInfo = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
            tablaInfo.addCell(Cell().add(Paragraph("Mascota: ${consulta.nombreMascota ?: "N/A"}")))
            tablaInfo.addCell(Cell().add(Paragraph("Especie: ${consulta.especieMascota ?: "N/A"}")))
            tablaInfo.addCell(Cell().add(Paragraph("Fecha: ${consulta.fechaConsulta ?: "N/A"}")))
            tablaInfo.addCell(Cell().add(Paragraph("Veterinario: ${consulta.nombreVeterinario ?: "N/A"}")))
            document.add(tablaInfo)

            document.add(Paragraph("\n"))

            // --- SECCIÓN 2: SIGNOS VITALES ---
            document.add(Paragraph("SIGNOS VITALES").setBold().setUnderline())
            val tablaSignos = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
            tablaSignos.addCell(Cell().add(Paragraph("Peso Actual: ${consulta.pesoActual ?: "---"} kg")))
            tablaSignos.addCell(Cell().add(Paragraph("Temperatura: ${consulta.temperatura ?: "---"} °C")))
            document.add(tablaSignos)

            document.add(Paragraph("\n"))

            // --- SECCIÓN 3: DIAGNÓSTICO ---
            document.add(Paragraph("MOTIVO DE CONSULTA:").setBold())
            document.add(Paragraph(consulta.motivo ?: "No especificado"))

            document.add(Paragraph("DIAGNÓSTICO:").setBold())
            document.add(Paragraph(consulta.diagnostico ?: "Pendiente de evaluación"))

            document.add(Paragraph("\n"))

            // --- SECCIÓN 4: TRATAMIENTO DETALLADO ---
            document.add(Paragraph("PLAN DE TRATAMIENTO").setBold().setUnderline())
            val tablaPlan = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f))).useAllAvailableWidth()

            tablaPlan.addCell(Cell().add(Paragraph("Medicamento:").setBold()))
            tablaPlan.addCell(Cell().add(Paragraph("${consulta.nombreMedicamento} (${consulta.tipoMedicamento ?: "N/A"})")))

            tablaPlan.addCell(Cell().add(Paragraph("Dosis:").setBold()))
            tablaPlan.addCell(Cell().add(Paragraph(consulta.dosis ?: "N/A")))

            tablaPlan.addCell(Cell().add(Paragraph("Frecuencia:").setBold()))
            tablaPlan.addCell(Cell().add(Paragraph(consulta.frecuencia ?: "N/A")))
            tablaPlan.addCell(Cell().add(Paragraph("Duración:").setBold()))
            tablaPlan.addCell(Cell().add(Paragraph("${consulta.duracion ?: "N/A"} (Inicia: ${consulta.fechaInicio ?: "N/A"})")))
            document.add(tablaPlan)
            document.add(Paragraph("\nRECOMENDACIONES ADICIONALES:").setBold())
            document.add(Paragraph(consulta.recomendaciones ?: "Ninguna"))
            document.add(Paragraph("\n\n--------------------------------------").setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))
            document.add(Paragraph("Firma del Veterinario Responsable").setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER))

            document.close()
            abrirPdf(file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error al crear PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun abrirPdf(file: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Abrir con..."))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
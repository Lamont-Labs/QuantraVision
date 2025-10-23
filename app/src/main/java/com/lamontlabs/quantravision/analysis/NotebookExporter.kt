package com.lamontlabs.quantravision.analysis

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter

/**
 * NotebookExporter
 * Converts a recorded session to a deterministic PDF "trading notebook".
 * Educational, offline, reproducible.
 */
object NotebookExporter {

    private val fmt = SimpleDateFormat("yyyy-MM-dd'T'HHmmss'Z'", Locale.US)

    fun export(context: Context, sessionDir: File): File {
        val outDir = File(context.filesDir, "dist").apply { mkdirs() }
        val outFile = File(outDir, "session_${sessionDir.name}.pdf")

        val doc = Document(PageSize.A4)
        PdfWriter.getInstance(doc, outFile.outputStream())
        doc.open()
        val fontTitle = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
        val fontBody = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL)

        doc.add(Paragraph("QuantraVision Session Report", fontTitle))
        doc.add(Paragraph("Session: ${sessionDir.name}", fontBody))
        doc.add(Paragraph("Generated: ${fmt.format(Date())}", fontBody))
        doc.add(Paragraph(" ", fontBody))

        val log = File(sessionDir, "detections.log")
        if (log.exists()) {
            doc.add(Paragraph("Detections:", fontTitle))
            log.forEachLine { line ->
                doc.add(Paragraph(line, fontBody))
            }
        }

        doc.add(Paragraph(" ", fontBody))
        doc.add(Paragraph("⚠ Illustrative Only — Not Financial Advice", fontBody))
        doc.close()
        return outFile
    }
}

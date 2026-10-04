package com.example.candbsee.file

import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.SwingUtilities

actual fun openDbcFileDialog(onFileLoaded: (fileName: String, content: String) -> Unit) {
    SwingUtilities.invokeLater {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Open CAN Database (DBC) File"
            fileFilter = FileNameExtensionFilter("CAN Database Files (*.dbc)", "dbc")
            isAcceptAllFileFilterUsed = true
        }
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            try {
                val content = file.readText()
                onFileLoaded(file.name, content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

actual fun saveDbcFile(fileName: String, content: String) {
    SwingUtilities.invokeLater {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Save CAN Database (DBC) File"
            selectedFile = File(fileName)
            fileFilter = FileNameExtensionFilter("CAN Database Files (*.dbc)", "dbc")
        }
        val result = fileChooser.showSaveDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            var file = fileChooser.selectedFile
            if (!file.name.endsWith(".dbc", ignoreCase = true)) {
                file = File(file.parentFile, "${file.name}.dbc")
            }
            try {
                file.writeText(content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

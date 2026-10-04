@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
package com.example.candbsee.file

import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import kotlinx.browser.document
import org.w3c.dom.events.Event

actual fun openDbcFileDialog(onFileLoaded: (fileName: String, content: String) -> Unit) {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = ".dbc,.txt"
    input.onchange = { _: Event ->
        val files = input.files
        if (files != null && files.length > 0) {
            val file = files.item(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = { _: Event ->
                    val text = reader.result?.toString() ?: ""
                    onFileLoaded(file.name, text)
                }
                reader.readAsText(file)
            }
        }
    }
    input.click()
}

actual fun saveDbcFile(fileName: String, content: String) {
    val downloadName = if (fileName.endsWith(".dbc")) fileName else "$fileName.dbc"
    downloadTextFileWasm(downloadName, content)
}

private fun downloadTextFileWasm(fileName: String, content: String): Unit = js("""
{
    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}
""")

package com.example.candbsee.file

import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.FileReader
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlinx.browser.document

actual fun openDbcFileDialog(onFileLoaded: (fileName: String, content: String) -> Unit) {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.accept = ".dbc,.txt"
    input.onchange = { _ ->
        val file = input.files?.item(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = { _ ->
                val text = reader.result as? String ?: ""
                onFileLoaded(file.name, text)
            }
            reader.readAsText(file)
        }
    }
    input.click()
}

actual fun saveDbcFile(fileName: String, content: String) {
    val blob = Blob(arrayOf(content), BlobPropertyBag(type = "text/plain"))
    val url = URL.createObjectURL(blob)
    val a = document.createElement("a") as HTMLAnchorElement
    a.href = url
    a.download = if (fileName.endsWith(".dbc")) fileName else "$fileName.dbc"
    document.body?.appendChild(a)
    a.click()
    document.body?.removeChild(a)
    URL.revokeObjectURL(url)
}

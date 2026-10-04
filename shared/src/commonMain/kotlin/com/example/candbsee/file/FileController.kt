package com.example.candbsee.file

expect fun openDbcFileDialog(onFileLoaded: (fileName: String, content: String) -> Unit)
expect fun saveDbcFile(fileName: String, content: String)

package com.ancientlightstudios.quarkus.kotlin.openapi.strafbank

import java.io.BufferedWriter
import java.io.File

/**
 * Creates a file in the given root directory, with the given package name and class name.
 */
fun mkFile(root: String, packageName: String, className: String): File {
    val file = File("$root/${packageName.replace(".", "/")}/$className.kt")
    file.parentFile.mkdirs()
    check(file.exists() || file.createNewFile()) { "Could not create file $file" }
    return file
}

fun BufferedWriter.writeln(line: String) {
    write(line)
    newLine()
}

fun BufferedWriter.writeln() {
    newLine()
}
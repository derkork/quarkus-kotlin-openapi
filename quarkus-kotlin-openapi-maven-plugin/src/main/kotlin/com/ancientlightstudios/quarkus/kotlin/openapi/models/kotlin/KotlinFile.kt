package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinFile(private val content: KotlinFileContent, val packageName: String) {

    val fileName = content.name.name
    private val imports = mutableListOf<String>()

    fun addImport(value: String) {
        imports.add(value)
    }

    fun render(writer: CodeWriter) = with(writer) {
        writeln("package $packageName")
        writeln()
        imports.forEach {
            writeln("import $it")
        }
        writeln()
        content.render(this)
    }

}

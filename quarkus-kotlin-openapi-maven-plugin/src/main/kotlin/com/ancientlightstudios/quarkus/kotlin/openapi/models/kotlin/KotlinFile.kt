package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinFile(private val content: KotlinFileContent, val packageName: String) {

    val fileName = content.name.name
    val imports = mutableSetOf<String>()

    fun render(writer: CodeWriter) = with(writer) {
        writeln("// THIS IS A GENERATED FILE. DO NOT EDIT!")
        writeln("package $packageName")
        writeln()
        if (imports.isNotEmpty()) {
            imports.forEach {
                writeln("import $it")
            }
            writeln()
        }
        content.render(this)
    }

}

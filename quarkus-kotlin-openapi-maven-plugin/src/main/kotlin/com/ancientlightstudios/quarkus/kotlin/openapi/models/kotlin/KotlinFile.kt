package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.strafbank.CodeWriter

class KotlinFile(val content:KotlinFileContent, val packageName: String, val imports: List<String>) {

    val fileName = content.name.name

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

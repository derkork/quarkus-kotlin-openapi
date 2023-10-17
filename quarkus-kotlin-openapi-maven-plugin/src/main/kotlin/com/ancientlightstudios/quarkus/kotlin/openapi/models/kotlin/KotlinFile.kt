package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinFile(val packageName: String, val fileName: ClassName) {

    private val imports = mutableSetOf<String>()
    private val content = mutableListOf<KotlinFileContent>()

    fun registerImport(import: String) = apply { imports.add(import) }

    fun addFileContent(content: KotlinFileContent) = apply { this.content.add(content) }

    fun render(writer: CodeWriter) = with(writer) {
        writeln("// THIS IS A GENERATED FILE. DO NOT EDIT!")
        writeln("package $packageName")

        if (imports.isNotEmpty()) {
            writeln()
            imports.sorted().forEach {
                writeln("import $it")
            }
        }

        if (content.isNotEmpty()) {
            writeln()
            content.forEachWithStats { status, item ->
                item.render(this)
                if (!status.last) {
                    writeln(forceNewLine = false) // in case the item already rendered a line break
                    writeln()
                }
            }
        }

    }
}

fun kotlinFile(packageName: String, fileName: ClassName, block: KotlinFile.() -> Unit) =
    KotlinFile(packageName, fileName).apply(block)
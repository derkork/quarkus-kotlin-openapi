package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.renderParameterBlock
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinClass(name: ClassName) : KotlinFileContent(name) {

    val parameters = mutableListOf<KotlinMember>()

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)

        write("class ${name.name}")

        if (parameters.isNotEmpty()) {
            write("(")
            renderParameterBlock(parameters) { it.render(this) }
            write(")")
        }

        writeln(" {")
        indent {
            writeln()
            methods.forEach {
                it.render(this)
                writeln()
            }
        }
        write("}")
    }
}

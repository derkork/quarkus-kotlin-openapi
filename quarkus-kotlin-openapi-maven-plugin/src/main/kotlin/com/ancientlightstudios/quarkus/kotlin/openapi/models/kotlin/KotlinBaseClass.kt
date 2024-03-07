package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class KotlinBaseClass(private val name: ClassName, private vararg val parameter: KotlinExpression) : KotlinRenderable {

    override fun ImportCollector.registerImports() {
        this.registerFrom(parameter.toList())
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write(name.value)

        if (parameter.isNotEmpty()) {
            write("(")
        }

        parameter.forEachWithStats { status, it ->
            it.render(this)
            if (!status.last) {
                write(", ")
            }
        }

        if (parameter.isNotEmpty()) {
            write(")")
        }
    }

}
package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinMethod(
    private val name: Name.MethodName,
    private val returnType: Name.ClassName?,
    private val parameters: List<KotlinParameter>,
    private val body: KotlinCode? = null
) {
    val annotations = KotlinAnnotationContainer()

    fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)
        write("fun ${name.name}(")
        parameters.forEach {
            it.render(this)
            write(", ")
        }
        write(")")

        if (returnType != null) {
            write(": ${returnType.name}")
        }

        if (body != null) {
            writeln(" {")
            indent {
                body.render(this)
            }
            writeln("}")
        } else {
            writeln()
        }
    }
}
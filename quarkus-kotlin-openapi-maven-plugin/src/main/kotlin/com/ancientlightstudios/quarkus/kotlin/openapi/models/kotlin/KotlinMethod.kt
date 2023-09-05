package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinMethod(
    private val name: MethodName,
    private val suspend: Boolean,
    private val returnType: ClassName?,
    private val body: KotlinCode? = null
) {

    val annotations = KotlinAnnotationContainer()
    val parameters = mutableListOf<KotlinParameter>()

    fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)
        if (suspend) {
            write("suspend ")
        }

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
package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinMember(
    private val name: VariableName,
    private val type: ClassName,
    private val nullable: Boolean = false,
    private val mutable: Boolean = false,
    private val private: Boolean = true
) {

    val annotations = KotlinAnnotationContainer()

    fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, false)
        if (private) {
            write("private ")
        }
        write(if (mutable) "var" else "val")
        write(" ${name.name}: ${type.name}")
        if (nullable) {
            write("?")
        }
    }

}

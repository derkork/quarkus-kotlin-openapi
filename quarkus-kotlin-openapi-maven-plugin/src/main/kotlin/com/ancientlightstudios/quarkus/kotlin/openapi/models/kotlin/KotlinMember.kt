package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinMember(
    private val name: VariableName,
    private val type: TypeName,
    private val mutable: Boolean = false,
    private val private: Boolean = true
) : AnnotationAware {

    private val annotations = KotlinAnnotationContainer()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)
        if (private) {
            write("private ")
        }
        write(if (mutable) "var" else "val")
        write(" ${name.render()}: ${type.render()}")
    }

}

interface MemberAware {

    fun addMember(member: KotlinMember)

}

fun MemberAware.kotlinMember(
    name: VariableName,
    type: TypeName,
    mutable: Boolean = false,
    private: Boolean = true,
    block: KotlinMember.() -> Unit = {}
) {
    val content = KotlinMember(name, type, mutable, private).apply(block)
    addMember(content)

}
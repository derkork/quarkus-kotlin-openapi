package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class KotlinMember(
    private val name: VariableName,
    private val type: TypeName,
    private val mutable: Boolean = false,
    private val private: Boolean = true,
    private val open: Boolean = false,
    private val override: Boolean = false,
    private val default: Expression? = null,
    val initializedInConstructor: Boolean = true
) : KotlinRenderable, AnnotationAware {

    private val annotations = KotlinAnnotationContainer()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun render(writer: CodeWriter): Unit = with(writer) {
        annotations.render(this, true)
        if (private) {
            write("private ")
        }

        if (open) {
            write("open ")
        }

        if (override) {
            write("override ")
        }

        write(if (mutable) "var" else "val")
        write(" ${name.render()}: ${type.render()}")
        default?.let { write(" = ${it.evaluate()}") }
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
    open: Boolean = false,
    override: Boolean = false,
    default: Expression? = null,
    initializedInConstructor: Boolean = true,
    block: KotlinMember.() -> Unit = {}
) {
    val content = KotlinMember(name, type, mutable, private, open, override, default, initializedInConstructor).apply(block)
    addMember(content)

}
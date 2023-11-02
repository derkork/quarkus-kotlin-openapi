package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class KotlinMember(
    private val name: VariableName,
    private val type: TypeName,
    private val mutable: Boolean = false,
    private val accessModifier: KotlinAccessModifier? = KotlinAccessModifier.Private,
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
        accessModifier?.let { write("${it.value} ") }

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
    accessModifier: KotlinAccessModifier? = KotlinAccessModifier.Private,
    open: Boolean = false,
    override: Boolean = false,
    default: Expression? = null,
    initializedInConstructor: Boolean = true,
    block: KotlinMember.() -> Unit = {}
) {
    val content = KotlinMember(name, type, mutable, accessModifier, open, override, default, initializedInConstructor).apply(block)
    addMember(content)

}
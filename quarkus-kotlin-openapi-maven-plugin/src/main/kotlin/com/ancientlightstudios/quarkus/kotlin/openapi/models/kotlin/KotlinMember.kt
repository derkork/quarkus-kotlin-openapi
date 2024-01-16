package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinMember(
    private val name: VariableName,
    private val type: TypeName,
    private val mutable: Boolean = false,
    private val accessModifier: KotlinAccessModifier? = KotlinAccessModifier.Private,
    private val open: Boolean = false,
    private val override: Boolean = false,
    private val default: KotlinExpression? = null,
    val initializedInConstructor: Boolean = true
) : KotlinRenderable, AnnotationAware {

    private val annotations = KotlinAnnotationContainer(true)

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun ImportCollector.registerImports() {
        register(type)
        default?.let { registerFrom(it) }
        registerFrom(annotations)
    }

    override fun render(writer: CodeWriter): Unit = with(writer) {
        annotations.render(this)
        accessModifier?.let { write("${it.value} ") }

        if (open) {
            write("open ")
        }

        if (override) {
            write("override ")
        }

        write(if (mutable) "var" else "val")
        write(" ${name.value}: ${type.value}")
        default?.let {
            write(" = ")
            it.render(this)
        }
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
    default: KotlinExpression? = null,
    initializedInConstructor: Boolean = true,
    block: KotlinMember.() -> Unit = {}
) {
    val content = KotlinMember(
        name,
        type,
        mutable,
        accessModifier,
        open,
        override,
        default,
        initializedInConstructor
    ).apply(block)
    addMember(content)

}
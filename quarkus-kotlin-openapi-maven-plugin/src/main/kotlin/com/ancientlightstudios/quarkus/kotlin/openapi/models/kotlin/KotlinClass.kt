package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.ExtendExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName

class KotlinClass(
    private val name: ClassName,
    private val privateConstructor: Boolean = false,
    private val asDataClass: Boolean = false,
    private val sealed: Boolean = false,
    private val extends: List<ExtendExpression> = emptyList()
) : KotlinRenderable, AnnotationAware, MethodAware, MemberAware, CompanionAware, CommentAware, ClassAware {

    private val annotations = KotlinAnnotationContainer()
    private val items = KotlinRenderableBlockContainer<KotlinRenderable>()

    private val constructorMembers = KotlinRenderableWrapContainer<KotlinMember>()
    private val otherMembers = KotlinRenderableBlockContainer<KotlinMember>(separateItemsWithNewLine = false)
    private var companion: KotlinCompanion? = null
    private var comment: KotlinComment? = null

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addMethod(method: KotlinMethod) {
        items.addItem(method)
    }

    override fun addClass(clazz: KotlinClass) {
        items.addItem(clazz)
    }

    override fun addMember(member: KotlinMember) {
        when(member.initializedInConstructor) {
            true -> constructorMembers.addItem(member)
            false -> otherMembers.addItem(member)
        }
    }

    override fun setCompanion(companion: KotlinCompanion) {
        this.companion = companion
    }

    override fun setComment(comment: KotlinComment) {
        this.comment = comment
    }

    override fun render(writer: CodeWriter) = with(writer) {
        comment?.let {
            it.render(this)
            writeln(forceNewLine = false)
        }

        annotations.render(this)
        if (sealed) {
            write("sealed ")
        }

        if (asDataClass) {
            write("data ")
        }

        write("class ${name.render()}")

        if (privateConstructor) {
            write(" private constructor")
        }

        if (constructorMembers.isNotEmpty || privateConstructor) {
            write("(")
            constructorMembers.render(this)
            write(")")
        }

        if (extends.isNotEmpty()) {
            write(extends.joinToString(prefix = " : ") { it.evaluate() })
        }

        if (otherMembers.isNotEmpty || items.isNotEmpty || companion != null) {
            writeln(" {")
            indent {

                if (otherMembers.isNotEmpty) {
                    writeln()
                    otherMembers.render(this)
                    writeln(forceNewLine = false) // in case the item already rendered a line break
                }

                if (items.isNotEmpty) {
                    writeln()
                    items.render(this)
                    writeln(forceNewLine = false) // in case the item already rendered a line break
                }

                companion?.let {
                    writeln()
                    it.render(this)
                    writeln(forceNewLine = false) // in case the item already rendered a line break
                }

                writeln()
            }
            write("}")
        }
    }

}

interface ClassAware {

    fun addClass(clazz: KotlinClass)

}

fun ClassAware.kotlinClass(
    name: ClassName,
    privateConstructor: Boolean = false,
    asDataClass: Boolean = false,
    sealed: Boolean = false,
    extends: List<ExtendExpression> = emptyList(),
    block: KotlinClass.() -> Unit
) {
    val content = KotlinClass(name, privateConstructor, asDataClass, sealed, extends).apply(block)
    addClass(content)
}

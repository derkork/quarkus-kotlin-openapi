package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinClass(
    private val name: ClassName,
    private val constructorAccessModifier: KotlinAccessModifier? = null,
    private val asDataClass: Boolean = false,
    private val sealed: Boolean = false,
    private val baseClass: KotlinBaseClass? = null,
    private val interfaces: List<ClassName> = emptyList()
) : KotlinRenderable, AnnotationAware, MethodAware, MemberAware, CompanionAware,
    CommentAware, ClassAware, ConstructorAware {

    private val annotations = KotlinAnnotationContainer()
    private val items = KotlinRenderableBlockContainer<KotlinRenderable>()

    private val constructorMembers = KotlinRenderableWrapContainer<KotlinMember>()
    private val otherMembers = KotlinRenderableBlockContainer<KotlinMember>(separateItemsWithEmptyLine = false)
    private var companion: KotlinCompanion? = null
    private var comment: KotlinComment? = null
    private val additionalConstructors = KotlinRenderableBlockContainer<KotlinConstructor>()

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
        when (member.initializedInConstructor) {
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

    override fun addConstructor(constructor: KotlinConstructor) {
        additionalConstructors.addItem(constructor)
    }

    override fun ImportCollector.registerImports() {
        baseClass?.let { registerFrom(it) }
        register(interfaces)
        registerFrom(annotations)
        registerFrom(items)
        registerFrom(constructorMembers)
        registerFrom(otherMembers)
        companion?.let { registerFrom(it) }
        registerFrom(additionalConstructors)
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

        write("class ${name.value}")

        constructorAccessModifier?.let { write(" ${it.value} constructor") }

        if (constructorMembers.isNotEmpty || constructorAccessModifier != null) {
            write("(")
            constructorMembers.render(this)
            write(")")
        }

        if (baseClass != null || interfaces.isNotEmpty()) {
            write(" : ")
            baseClass?.render(this)

            if (baseClass != null && interfaces.isNotEmpty()) {
                write(", ")
            }

            if (interfaces.isNotEmpty()) {
                write(interfaces.joinToString { it.value })
            }
        }

        if (otherMembers.isNotEmpty || items.isNotEmpty || companion != null || additionalConstructors.isNotEmpty) {
            write(" ")
            block {
                if (otherMembers.isNotEmpty) {
                    writeln()
                    otherMembers.render(this)
                    writeln(forceNewLine = false) // in case the item already rendered a line break
                }

                if (additionalConstructors.isNotEmpty) {
                    writeln()
                    additionalConstructors.render(this)
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
        }
    }

}

interface ClassAware {

    fun addClass(clazz: KotlinClass)

}

fun ClassAware.kotlinClass(
    name: ClassName,
    constructorAccessModifier: KotlinAccessModifier? = null,
    asDataClass: Boolean = false,
    sealed: Boolean = false,
    baseClass: KotlinBaseClass? = null,
    interfaces: List<ClassName> = emptyList(),
    block: KotlinClass.() -> Unit
) {
    val content = KotlinClass(name, constructorAccessModifier, asDataClass, sealed, baseClass, interfaces).apply(block)
    addClass(content)
}

package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinInterface(
    private val name: ClassName,
    private val sealed: Boolean = false,
    private val interfaces: List<ClassName> = listOf()
) : KotlinRenderable, AnnotationAware, MethodAware, CommentAware, ClassAware, CompanionAware {

    private val annotations = KotlinAnnotationContainer()
    private val methods = KotlinRenderableBlockContainer<KotlinMethod>()
    private var comment: KotlinComment? = null
    private val items = KotlinRenderableBlockContainer<KotlinRenderable>()
    private var companion: KotlinCompanion? = null

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addMethod(method: KotlinMethod) {
        methods.addItem(method)
    }

    override fun setComment(comment: KotlinComment) {
        this.comment = comment
    }

    override fun setCompanion(companion: KotlinCompanion) {
        this.companion = companion
    }

    override fun addClass(clazz: KotlinClass) {
        items.addItem(clazz)
    }

    override fun ImportCollector.registerImports() {
        register(interfaces)
        registerFrom(annotations)
        registerFrom(methods)
        registerFrom(items)
        companion?.let { registerFrom(it) }
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
        write("interface ${name.value}")

        if (interfaces.isNotEmpty()) {
            write(interfaces.joinToString(prefix = " : ") { it.value })
        }

        if (methods.isNotEmpty || items.isNotEmpty || companion != null) {
            write(" ")
            block {
                if (items.isNotEmpty) {
                    writeln()
                    items.render(this)
                    writeln(forceNewLine = false)
                }

                if (methods.isNotEmpty) {
                    writeln()
                    methods.render(this)
                    writeln(forceNewLine = false)
                }

                companion?.let {
                    writeln()
                    it.render(this)
                    writeln(forceNewLine = false) // in case the item already rendered a line break
                }
            }
        }
    }

}

interface InterfaceAware {

    fun addInterface(interfaze: KotlinInterface)

}

fun InterfaceAware.kotlinInterface(
    name: ClassName,
    sealed: Boolean = false,
    interfaces: List<ClassName> = listOf(),
    block: KotlinInterface.() -> Unit
) {
    val content = KotlinInterface(name, sealed, interfaces).apply(block)
    addInterface(content)
}

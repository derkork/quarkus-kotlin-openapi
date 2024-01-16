package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinInterface(
    private val name: ClassName,
    private val sealed: Boolean = false,
    private val interfaces: List<ClassName> = emptyList()
) : KotlinRenderable, AnnotationAware, MethodAware, CommentAware, ClassAware {

    private val annotations = KotlinAnnotationContainer()
    private val methods = KotlinRenderableBlockContainer<KotlinMethod>()
    private var comment: KotlinComment? = null
    private val items = KotlinRenderableBlockContainer<KotlinRenderable>()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addMethod(method: KotlinMethod) {
        methods.addItem(method)
    }

    override fun setComment(comment: KotlinComment) {
        this.comment = comment
    }


    override fun addClass(clazz: KotlinClass) {
        items.addItem(clazz)
    }

    override fun ImportCollector.registerImports() {
        register(interfaces)
        registerFrom(annotations)
        registerFrom(methods)
        registerFrom(items)
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

        if (methods.isNotEmpty || items.isNotEmpty) {
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
    interfaces: List<ClassName> = emptyList(),
    block: KotlinInterface.() -> Unit
) {
    val content = KotlinInterface(name, sealed, interfaces).apply(block)
    addInterface(content)
}

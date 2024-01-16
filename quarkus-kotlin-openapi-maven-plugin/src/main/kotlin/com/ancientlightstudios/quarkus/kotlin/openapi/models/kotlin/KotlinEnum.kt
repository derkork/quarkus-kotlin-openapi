package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

class KotlinEnum(private val name: ClassName) : KotlinRenderable,
    AnnotationAware, MethodAware, MemberAware, CommentAware {

    private val annotations = KotlinAnnotationContainer()
    private val methods = KotlinRenderableBlockContainer<KotlinMethod>()
    private val members = KotlinRenderableWrapContainer<KotlinMember>()
    private val items = mutableListOf<KotlinEnumItem>()
    private var comment: KotlinComment? = null

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addMethod(method: KotlinMethod) {
        methods.addItem(method)
    }

    override fun addMember(member: KotlinMember) {
        members.addItem(member)
    }

    fun addItem(item: KotlinEnumItem) {
        items.add(item)
    }

    override fun setComment(comment: KotlinComment) {
        this.comment = comment
    }

    override fun ImportCollector.registerImports() {
        registerFrom(annotations)
        registerFrom(methods)
        registerFrom(members)
        registerFrom(items)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        comment?.let {
            it.render(this)
            writeln(forceNewLine = false)
        }

        annotations.render(this)
        write("enum class ${name.value}")
        if (members.isNotEmpty) {
            write("(")
            members.render(this)
            write(")")
        }

        write(" ")
        block {
            items.forEachWithStats { status, item ->
                item.render(this)
                if (!status.last) {
                    writeln(",")
                } else {
                    writeln(";")
                }
            }

            if (methods.isNotEmpty) {
                writeln()
                methods.render(this)
            }
        }
    }

}

interface EnumAware {

    fun addEnum(enum: KotlinEnum)

}

fun EnumAware.kotlinEnum(name: ClassName, block: KotlinEnum.() -> Unit) {
    val content = KotlinEnum(name).apply(block)
    addEnum(content)
}

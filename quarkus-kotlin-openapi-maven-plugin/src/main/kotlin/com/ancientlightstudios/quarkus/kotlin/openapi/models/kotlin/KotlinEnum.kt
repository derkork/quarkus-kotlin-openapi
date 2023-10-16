package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinEnum(private val name: ClassName) : KotlinFileContent,
    AnnotationAware, MethodAware, MemberAware {

    private val annotations = KotlinAnnotationContainer()
    private val methods = KotlinMethodContainer()
    private val members = KotlinMemberContainer()
    private val items = mutableListOf<KotlinEnumItem>()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addMethod(method: KotlinMethod) {
        methods.addMethod(method)
    }

    override fun addMember(member: KotlinMember) {
        members.addMember(member)
    }

    fun addItem(item: KotlinEnumItem) {
        items.add(item)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this)

        write("enum class ${name.render()}")
        if (members.isNotEmpty) {
            write("(")
            members.render(this)
            write(")")
        }

        write(" {")
        indent(newLineBefore = true, newLineAfter = true) {
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

        write("}")
    }

}

fun KotlinFile.kotlinEnum(name: ClassName, block: KotlinEnum.() -> Unit) {
    val content = KotlinEnum(name).apply(block)
    addFileContent(content)
}

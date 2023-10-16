package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class KotlinClass(private val name: ClassName, private val privateConstructor: Boolean = false) : KotlinFileContent,
    AnnotationAware, MethodAware, MemberAware, CompanionAware {

    private val annotations = KotlinAnnotationContainer()
    private val methods = KotlinMethodContainer()
    private val members = KotlinMemberContainer()
    private var companion: KotlinCompanion? = null

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addMethod(method: KotlinMethod) {
        methods.addMethod(method)
    }

    override fun addMember(member: KotlinMember) {
        members.addMember(member)
    }

    override fun setCompanion(companion: KotlinCompanion) {
        this.companion = companion
    }

    override fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this)
        write("class ${name.render()}")

        if (privateConstructor) {
            write(" private constructor")
        }

        if (members.isNotEmpty || privateConstructor) {
            write("(")
            members.render(this)
            write(")")
        }

        if (methods.isNotEmpty || companion != null) {
            writeln(" {")
            indent {

                if (methods.isNotEmpty) {
                    writeln()
                    methods.render(this)
                    writeln(forceNewLine = false) // in case the item already rendered a line break
                }

                companion?.let {
                    writeln()
                    companion!!.render(this)
                    writeln(forceNewLine = false) // in case the item already rendered a line break
                }

                writeln()
            }
            write("}")
        }
    }

}

fun KotlinFile.kotlinClass(name: ClassName, privateConstructor: Boolean = false, block: KotlinClass.() -> Unit) {
    val content = KotlinClass(name, privateConstructor).apply(block)
    addFileContent(content)
}

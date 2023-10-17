package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName

class KotlinClass(
    private val name: ClassName,
    private val privateConstructor: Boolean = false,
    private val asDataClass: Boolean = false
) : KotlinFileContent, AnnotationAware, MethodAware, MemberAware, CompanionAware, CommentAware {

    private val annotations = KotlinAnnotationContainer()
    private val methods = KotlinMethodContainer()
    private val members = KotlinMemberContainer()
    private var companion: KotlinCompanion? = null
    private var comment: KotlinComment? = null

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

    override fun setComment(comment: KotlinComment) {
        this.comment = comment
    }

    override fun render(writer: CodeWriter) = with(writer) {
        comment?.let {
            it.render(this)
            writeln(forceNewLine = false)
        }

        annotations.render(this)
        if (asDataClass) {
            write("data ")
        }

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
                    it.render(this)
                    writeln(forceNewLine = false) // in case the item already rendered a line break
                }

                writeln()
            }
            write("}")
        }
    }

}

fun KotlinFile.kotlinClass(
    name: ClassName,
    privateConstructor: Boolean = false,
    asDataClass: Boolean = false,
    block: KotlinClass.() -> Unit
) {
    val content = KotlinClass(name, privateConstructor, asDataClass).apply(block)
    addFileContent(content)
}

package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

// TODO: replace suspend, private and other stuff with a bit flag .e.g. Suspend | Private to avoid 5 more members for internal etc
class KotlinMethod(
    private val name: MethodName,
    private val suspend: Boolean = false,
    private val returnType: TypeName? = null,
    private val receiverType: TypeName? = null,
    private val bodyAsAssignment: Boolean = false,
    private val private: Boolean = false,
) : KotlinRenderable, AnnotationAware, ParameterAware, StatementAware, CommentAware {

    private val annotations = KotlinAnnotationContainer()
    private val parameters = KotlinRenderableWrapContainer<KotlinParameter>()
    private val statements = KotlinRenderableBlockContainer<KotlinStatement>(false)
    private var comment: KotlinComment? = null

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addParameter(parameter: KotlinParameter) {
        parameters.addItem(parameter)
    }

    override fun addStatement(statement: KotlinStatement) {
        statements.addItem(statement)
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
        if (private) {
            write("private ")
        }

        if (suspend) {
            write("suspend ")
        }

        write("fun ")
        if (receiverType != null) {
            write("${receiverType.render()}.")
        }
        write("${name.render()}(")
        parameters.render(this)
        write(")")

        if (returnType != null) {
            write(": ${returnType.render()}")
        }

        if (statements.isNotEmpty) {
            if (!bodyAsAssignment) {
                writeln(" {")
            } else {
                write(" = ")
            }
            indent {
                statements.render(this)
            }
            writeln(forceNewLine = false) // in case the item already rendered a line break

            if (!bodyAsAssignment) {
                write("}")
            }
        }
    }

}

interface MethodAware {

    fun addMethod(method: KotlinMethod)

}

fun MethodAware.kotlinMethod(
    name: MethodName,
    suspend: Boolean = false,
    returnType: TypeName? = null,
    receiverType: TypeName? = null,
    bodyAsAssignment: Boolean = false,
    private: Boolean = false,
    block: KotlinMethod.() -> Unit = {}
) {
    val content = KotlinMethod(name, suspend, returnType, receiverType, bodyAsAssignment, private).apply(block)
    addMethod(content)

}

package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.MethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

// TODO: replace suspend, private and other stuff with a bit flag .e.g. Suspend | Private to avoid 5 more members for internal etc
class KotlinMethod(
    private val name: MethodName,
    private val suspend: Boolean = false,
    private val returnType: TypeName? = null,
    private val receiverType: TypeName? = null,
    private val bodyAsAssignment: Boolean = false,
    private val private: Boolean = false,
) : KotlinFileContent, AnnotationAware, ParameterAware, StatementAware {

    private val annotations = KotlinAnnotationContainer()
    private val parameters = KotlinParameterContainer()
    private val statements = KotlinStatementContainer()

    override fun addAnnotation(annotation: KotlinAnnotation) {
        annotations.addAnnotation(annotation)
    }

    override fun addParameter(parameter: KotlinParameter) {
        parameters.addParameter(parameter)
    }

    override fun addStatement(statement: KotlinStatement) {
        statements.addStatement(statement)
    }

    override fun render(writer: CodeWriter) = with(writer) {
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

fun KotlinFile.kotlinMethod(
    name: MethodName,
    suspend: Boolean = false,
    returnType: TypeName? = null,
    receiverType: TypeName? = null,
    bodyAsAssignment: Boolean = false,
    private: Boolean = false,
    block: KotlinMethod.() -> Unit = {}
) {
    val content = KotlinMethod(name, suspend, returnType, receiverType, bodyAsAssignment, private).apply(block)
    addFileContent(content)
}

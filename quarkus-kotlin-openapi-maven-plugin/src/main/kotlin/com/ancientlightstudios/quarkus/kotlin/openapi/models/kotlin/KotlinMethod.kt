package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.transformer.renderParameterBlock
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class KotlinMethod(
    private val name: MethodName,
    private val suspend: Boolean,
    private val returnType: TypeName? = null,
    private val receiverType: TypeName? = null,
    private val bodyAsAssignment: Boolean = false
) {

    val annotations = KotlinAnnotationContainer()
    private val parameters = mutableListOf<KotlinParameter>()
    private var body: KotlinStatementList? = null

    fun render(writer: CodeWriter) = with(writer) {
        annotations.render(this, true)
        if (suspend) {
            write("suspend ")
        }

        write("fun ")
        if (receiverType != null) {
            receiverType.render(this)
            write(".")
        }
        write("${name.name}(")
        renderParameterBlock(parameters) { it.render(this) }
        write(")")

        if (returnType != null) {
            write(": ")
            returnType.render(this)
        }

        body?.let {
            if (bodyAsAssignment) {
                write(" = ")
            } else {
                writeln(" {")
            }
            indent {
                it.render(this)
            }
            if (!bodyAsAssignment) {
                write("}")
            }
        }
        writeln()
    }

    fun addStatement(statement: KotlinStatement): KotlinMethod {
        if (body == null) {
            body = KotlinStatementList()
        }
        body!!.statements.add(statement)
        return this
    }

    fun addParameter(variableName:VariableName, typeName:TypeName): KotlinParameter {
        val result = KotlinParameter(variableName, typeName)
        parameters.add(result)
        return result
    }

}
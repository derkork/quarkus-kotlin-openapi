package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.MethodName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.forEachWithStats

abstract class ObjectBuilderTransformStatement : KotlinStatement {

    private val parameters = mutableListOf<VariableName>()

    fun addParameter(maybeName: VariableName) {
        parameters.add(maybeName)
    }

    protected fun CodeWriter.renderTransformStatement(objectName: ClassName, context: Expression) {
        write("maybeAllOf(${context.evaluate()}")
        parameters.forEach { maybeName ->
            write(", ${maybeName.render()}")
        }
        write(") {")
        indent(newLineBefore = true, newLineAfter = true) {
            write("${objectName.render()}(")
            indent(newLineBefore = true, newLineAfter = true) {
                parameters.forEachWithStats { status, maybeName ->
                    write("(${maybeName.render()} as Maybe.Success).value")
                    if (!status.last) {
                        writeln(",")
                    }
                }
            }
            write(")")
        }
        writeln("}")
    }

}

class SafeObjectBuilderTransformStatement(private val safeObject: ClassName) : ObjectBuilderTransformStatement() {

    override fun render(writer: CodeWriter) = with(writer) {
        write("return ")
        renderTransformStatement(safeObject, "context".variableName().pathExpression())
    }

}

class RequestBuilderTransformStatement(
    private val methodName: MethodName,
    private val requestContainer: ClassName?
) : ObjectBuilderTransformStatement() {

    override fun render(writer: CodeWriter) = with(writer) {
        if (requestContainer != null) {
            write("val request = ")
            renderTransformStatement(requestContainer, "request".stringExpression())
            writeln("return delegate.${methodName.render()}(request).response")
        } else {
            writeln("return delegate.${methodName.render()}().response")
        }
    }

}
package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class PrimitiveToMaybeTransformStatement(
    private val targetName: VariableName?, private val sourceName: Expression,
    private val context: StringExpression, private val type: ClassName, private val defaultValue: Expression?,
    private val required: Boolean
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        targetName?.let { write("val ${it.render()} = ") }
        renderPrimitiveTransformStatement(sourceName, context, type, defaultValue, required)
    }

}

class NestedPrimitiveTransformStatement(
    private val sourceName: Expression, private val type: ClassName,
    private val required: Boolean
) : KotlinStatement {

    override fun render(writer: CodeWriter) = writer.renderPrimitiveTransformStatement(
        sourceName, null, type, null, required
    )

}

fun CodeWriter.renderPrimitiveTransformStatement(
    sourceName: Expression, context: StringExpression?,
    type: ClassName, defaultValue: Expression?, required: Boolean
) {
    write("${sourceName.evaluate()}.as${type.render()}(")
    context?.let { write(it.evaluate()) }
    writeln(")")
    indent {
        // TODO: add .validateString { } or something like this if necessary (unless it's a shared primitive)
        if (defaultValue != null) {
            writeln(".default() { ${defaultValue.evaluate()} }")
        } else if (required) {
            writeln(".required()")
        }
    }
}
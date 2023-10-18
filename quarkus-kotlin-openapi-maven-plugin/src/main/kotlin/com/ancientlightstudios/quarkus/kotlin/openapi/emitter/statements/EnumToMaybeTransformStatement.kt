package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class EnumToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: StringExpression, private val type: ClassName, private val defaultValue: Expression?,
    private val required: Boolean
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${targetName.render()} = ")
        renderEnumTransformStatement(sourceName, context, type, defaultValue, required)
    }
}

class NestedEnumTransformStatement(
    private val sourceName: VariableName, private val type: ClassName, private val required: Boolean
) : KotlinStatement {

    override fun render(writer: CodeWriter) = writer.renderEnumTransformStatement(
        sourceName, null, type, null, required
    )
}

fun CodeWriter.renderEnumTransformStatement(
    sourceName: VariableName, context: StringExpression?,
    type: ClassName, defaultValue: Expression?, required: Boolean
) {
    write("${sourceName.render()}.as${type.render()}(")
    context?.let { write(it.evaluate()) }
    writeln(")")
    indent {
        // TODO: add validation. e.g. allowed values
        if (defaultValue != null) {
            writeln(".default() { ${defaultValue.evaluate()} }")
        } else if (required) {
            writeln(".required()")
        }
    }
}
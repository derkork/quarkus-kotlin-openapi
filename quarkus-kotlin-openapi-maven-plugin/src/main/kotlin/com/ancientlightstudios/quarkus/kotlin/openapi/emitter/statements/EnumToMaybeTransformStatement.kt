package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class EnumToMaybeTransformStatement(
    private val targetName: VariableName?,
    private val sourceName: Expression,
    private val context: StringExpression,
    private val type: ClassName,
    private val defaultValue: Expression?,
    private val required: Boolean,
    private val validation: Validation,
    private val valueTransform: (String) -> Expression
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        targetName?.let { write("val ${it.render()} = ") }
        renderEnumTransformStatement(sourceName, context, type, defaultValue, required, validation, valueTransform)
    }
}

class NestedEnumTransformStatement(
    private val sourceName: Expression, private val type: ClassName, private val required: Boolean,
    private val validation: Validation, private val valueTransform: (String) -> Expression
) : KotlinStatement {

    override fun render(writer: CodeWriter) = writer.renderEnumTransformStatement(
        sourceName, null, type, null, required, validation, valueTransform
    )
}

fun CodeWriter.renderEnumTransformStatement(
    sourceName: Expression, context: StringExpression?,
    type: ClassName, defaultValue: Expression?, required: Boolean,
    validation: Validation, valueTransform: (String) -> Expression
) {
    write("${sourceName.evaluate()}.as${type.render()}(")
    context?.let { write(it.evaluate()) }
    writeln(")")
    indent {
        render(valueTransform, validation)
        if (defaultValue != null) {
            writeln(".default() { ${defaultValue.evaluate()} }")
        } else if (required) {
            writeln(".required()")
        }
    }
}
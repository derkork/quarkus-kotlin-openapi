package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class PrimitiveToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: StringExpression, private val type: ClassName, private val defaultValue: Expression?,
    private val required: Boolean
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${targetName.render()} = ")
        NestedPrimitiveTransformStatement(sourceName, context, type, defaultValue, required).render(this)
    }
}

class NestedPrimitiveTransformStatement(
    private val sourceName: VariableName, private val context: StringExpression?,
    private val type: ClassName, private val defaultValue: Expression?, private val required: Boolean
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("${sourceName.render()}.as${type.render()}(")
        context?.let { write(it.evaluate()) }
        defaultValue?.let {
            write(", ${it.evaluate()}")
        }
        writeln(")")
        indent {
            // TODO: add .validateString { } or something like this if necessary (unless it's a shared primitive)
            if (required) {
                writeln(".required()")
            }
        }
    }
}
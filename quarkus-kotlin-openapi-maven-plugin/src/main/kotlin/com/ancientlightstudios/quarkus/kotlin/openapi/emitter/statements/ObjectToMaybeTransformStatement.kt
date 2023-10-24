package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class ObjectBodyToMaybeTransformStatement(
    private val targetName: VariableName?, private val sourceName: Expression,
    private val context: StringExpression, private val type: ClassName, private val required: Boolean,
    private val validation: Validation, private val valueTransform: (String) -> Expression
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        targetName?.let { write("val ${it.render()} = ") }
        writeln("${sourceName.evaluate()}.asObject(${context.evaluate()}, ${type.render()}::class.java, objectMapper)")
        indent {
            NestedObjectTransformStatement(null, type, required, validation, valueTransform).render(this)
        }
    }
}

class ObjectPropertyToMaybeTransformStatement(
    private val targetName: VariableName?, private val sourceName: Expression,
    private val context: StringExpression, private val type: ClassName, private val required: Boolean,
    private val validation: Validation, private val valueTransform: (String) -> Expression
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        targetName?.let { write("val ${it.render()} = ") }
        writeln("${sourceName.evaluate()}.asMaybe(${context.evaluate()})")
        indent {
            NestedObjectTransformStatement(null, type, required, validation, valueTransform).render(this)
        }
    }
}

class NestedObjectTransformStatement(
    private val sourceName: Expression?,
    private val type: ClassName,
    private val required: Boolean,
    private val validation: Validation,
    private val valueTransform: (String) -> Expression
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        sourceName?.let { write(it.evaluate()) }
        writeln(".validateUnsafe(${type.render()}::asSafe)")
        render(valueTransform, validation)
        if (required) {
            writeln(".required()")
        }

    }
}
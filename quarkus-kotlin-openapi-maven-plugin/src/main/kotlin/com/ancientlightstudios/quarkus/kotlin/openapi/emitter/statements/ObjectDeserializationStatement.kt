package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class ObjectDeserializationStatement(
    private val source: Expression, private val targetName: VariableName, private val type: ClassName,
    private val required: Boolean, private val validation: Validation,
    private val valueTransform: (String) -> Expression
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("val ${targetName.render()} = ${source.evaluate()}")
        indent {
            writeln(".asObject(::${type.render()})")
            writeln(".validateUnsafe(${type.render()}::asSafe)")
            render(valueTransform, validation)
            if (required) {
                writeln(".required()")
            }
        }
    }

}

class NestedObjectDeserializationStatement(
    private val source: Expression, private val type: ClassName,
    private val required: Boolean, private val validation: Validation,
    private val valueTransform: (String) -> Expression
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("${source.evaluate()}.asObject(::${type.render()})")
        indent {
            writeln(".validateUnsafe(${type.render()}::asSafe)")
            render(valueTransform, validation)
            if (required) {
                writeln(".required()")
            }
        }
    }

}

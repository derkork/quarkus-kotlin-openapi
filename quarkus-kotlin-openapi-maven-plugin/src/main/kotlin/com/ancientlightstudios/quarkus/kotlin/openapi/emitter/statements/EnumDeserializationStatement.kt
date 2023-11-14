package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class EnumDeserializationStatement(
    private val source: Expression, private val targetName: VariableName, private val type: ClassName,
    private val defaultValue: Expression?, private val required: Boolean,
    private val validation: Validation, private val valueTransform: (String) -> Expression
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("val ${targetName.render()} = ${source.evaluate()}")
        indent {
            writeln(".as${type.render()}()")
            render(valueTransform, validation)
            if (defaultValue != null) {
                writeln(".default() { ${defaultValue.evaluate()} }")
            } else if (required) {
                writeln(".required()")
            }
        }
    }

}

class NestedEnumDeserializationStatement(
    private val source: Expression, private val type: ClassName,
    private val required: Boolean, private val validation: Validation,
    private val valueTransform: (String) -> Expression
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("${source.evaluate()}.as${type.render()}()")
        indent {
            render(valueTransform, validation)
            if (required) {
                writeln(".required()")
            }
        }
    }

}
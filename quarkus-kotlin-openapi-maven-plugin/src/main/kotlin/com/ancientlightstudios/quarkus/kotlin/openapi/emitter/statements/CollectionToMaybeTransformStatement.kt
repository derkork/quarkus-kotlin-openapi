package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName

class CollectionBodyToMaybeTransformStatement(
    private val targetName: VariableName?, private val sourceName: Expression,
    private val context: StringExpression, private val type: TypeName,
    private val required: Boolean, private val validation: Validation,
    private val valueTransform: (String) -> Expression,
    private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        targetName?.let { write("val ${it.render()} = ") }
        writeln("${sourceName.evaluate()}.asList<${type.render()}>(${context.evaluate()}, objectMapper)")
        indent {
            NestedCollectionTransformStatement(null, required, validation, valueTransform, block).render(this)
        }
    }
}

class CollectionPropertyToMaybeTransformStatement(
    private val targetName: VariableName?, private val sourceName: Expression,
    private val context: StringExpression, private val required: Boolean,
    private val validation: Validation,
    private val valueTransform: (String) -> Expression,
    private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        targetName?.let { write("val ${it.render()} = ") }
        writeln("${sourceName.evaluate()}.asMaybe(${context.evaluate()})")
        indent {
            NestedCollectionTransformStatement(null, required, validation, valueTransform, block).render(this)
        }
    }
}

class NestedCollectionTransformStatement(
    private val sourceName: Expression?,
    private val required: Boolean,
    private val validation: Validation,
    private val valueTransform: (String) -> Expression,
    private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        sourceName?.let { write(it.evaluate()) }
        render(valueTransform, validation)
        writeln(".validateListItems {")
        indent {
            block("it".variableName()).render(this)
        }
        writeln("}")
        if (required) {
            writeln(".required()")
        }
    }
}

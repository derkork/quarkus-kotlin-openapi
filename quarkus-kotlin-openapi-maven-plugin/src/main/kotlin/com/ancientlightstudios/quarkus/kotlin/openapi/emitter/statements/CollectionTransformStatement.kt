package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.Expression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName

class CollectionTransformStatement(
    private val source: Expression, private val targetName: VariableName,
    private val required: Boolean, private val validation: Validation,
    private val valueTransform: (String) -> Expression,
    private val fromJsonNode: Boolean,
    private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("val ${targetName.render()} = ${source.evaluate()}")
        indent {
            if (fromJsonNode) {
                writeln(".asList()")
            }
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

}

class NestedCollectionTransformStatement(
    private val source: Expression, private val required: Boolean,
    private val validation: Validation, private val valueTransform: (String) -> Expression,
    private val fromJsonNode: Boolean,
    private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write(source.evaluate())
        if (fromJsonNode) {
            write(".asList()")
        }
        indent(newLineBefore = true) {
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

}

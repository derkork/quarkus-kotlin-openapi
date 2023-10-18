package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.TypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName.Companion.variableName

class CollectionBodyToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: StringExpression, private val type: TypeName,
    private val required: Boolean, private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${targetName.render()} = ${sourceName.render()}.asList<${type.render()}>(${context.evaluate()}, objectMapper)")
        indent {
            NestedCollectionTransformStatement(null, required, block).render(this)
        }
    }
}

class CollectionPropertyToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: StringExpression, private val required: Boolean,
    private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("val ${targetName.render()} = ${sourceName.render()}.asMaybe(${context.evaluate()})")
        indent {
            NestedCollectionTransformStatement(null, required, block).render(this)
        }
    }
}

class NestedCollectionTransformStatement(
    private val sourceName: VariableName?,
    private val required: Boolean,
    private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        // TODO: .validateList {} to check size constraint
        sourceName?.let { write(it.render()) }
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

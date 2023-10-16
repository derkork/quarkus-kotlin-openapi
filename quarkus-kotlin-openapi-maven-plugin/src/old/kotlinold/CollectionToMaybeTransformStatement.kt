package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

// val maybeBody = body.asList<List<RatingUnsafe?>>("request.body", objectMapper)

class CollectionBodyToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val type: TypeName, private val context: Expression, private val validationInfo: ValidationInfo,
    private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${targetName.name} = ${sourceName.name}.asList<")
        type.render(this)
        writeln(">(${context.expression}, objectMapper)")
        indent {
            NestedCollectionTransformStatement("".variableName(), validationInfo, block).render(this)
        }
    }
}

class CollectionPropertyToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: Expression, private val validationInfo: ValidationInfo,
    private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("val ${targetName.name} = ${sourceName.name}.asMaybe(${context.expression})")
        indent {
            NestedCollectionTransformStatement("".variableName(), validationInfo, block).render(this)
        }
    }
}

class NestedCollectionTransformStatement(
    private val sourceName: VariableName,
    private val validationInfo: ValidationInfo,
    private val block: (VariableName) -> KotlinStatement
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        // TODO: .validateList {} to check size constraint
        writeln("${sourceName.name}.validateListItems {")
        indent {
            block("it".variableName()).render(this)
        }
        writeln("}")
        if (validationInfo.required) {
            writeln(".required()")
        }
    }
}

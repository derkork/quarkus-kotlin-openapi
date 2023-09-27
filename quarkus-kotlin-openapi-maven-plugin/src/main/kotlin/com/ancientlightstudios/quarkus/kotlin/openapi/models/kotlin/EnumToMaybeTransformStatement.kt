package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class EnumToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: Expression, private val type: TypeName, private val validationInfo: ValidationInfo
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${targetName.name} = ")
        NestedEnumTransformStatement(sourceName, context, type, validationInfo).render(this)
    }
}

class NestedEnumTransformStatement(
    private val sourceName: VariableName, private val context: Expression?,
    private val type: TypeName, private val validationInfo: ValidationInfo
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("${sourceName.name}.as")
        type.render(this)
        write("(")
        context?.let { write(it.expression) }
        writeln(")")

        indent {
            // TODO: add validation. e.g. allowed values
            if (validationInfo.required) {
                writeln(".required()")
            }
        }
    }
}
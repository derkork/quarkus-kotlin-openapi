package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class PrimitiveToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: Expression, private val type: TypeName, private val defaultValue: String?,
    private val validationInfo: ValidationInfo
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${targetName.name} = ")
        NestedPrimitiveTransformStatement(sourceName, context, type, defaultValue, validationInfo).render(this)
    }
}

class NestedPrimitiveTransformStatement(
    private val sourceName: VariableName, private val context: Expression?, private val type: TypeName,
    private val defaultValue: String?, private val validationInfo: ValidationInfo
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        if (defaultValue != null && !validationInfo.required) {
            // TODO: we should replace this with a 'map { it ?: defaultValue }' to avoid parsing the value each time. but this class needs to know jow to convert the value into the required data type
            write("(${sourceName.name} ?: \"$defaultValue\").as")
        } else {
            write("${sourceName.name}.as")
        }
        type.render(this)
        write("(")
        context?.let { write(it.expression) }
        writeln(")")

        indent {
            // TODO: add .validateString { } or something like this if necessary (unless it's a shared primitive)
            if (validationInfo.required) {
                writeln(".required()")
            }
        }
    }
}
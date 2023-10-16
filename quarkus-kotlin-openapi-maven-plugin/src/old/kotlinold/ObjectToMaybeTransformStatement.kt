package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold.VariableName.Companion.variableName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.ValidationInfo
import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter

class ObjectBodyToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: Expression, private val type: TypeName, private val validationInfo: ValidationInfo
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${targetName.name} = ${sourceName.name}.asObject(${context.expression}, ")
        type.render(this)
        writeln("::class.java, objectMapper)")
        indent {
            NestedObjectTransformStatement("".variableName(), type, validationInfo).render(this)
        }
    }
}

class ObjectPropertyToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: Expression, private val type: TypeName, private val validationInfo: ValidationInfo
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("val ${targetName.name} = ${sourceName.name}.asMaybe(${context.expression})")
        indent {
            NestedObjectTransformStatement("".variableName(), type, validationInfo).render(this)
        }
    }
}

class NestedObjectTransformStatement(
    private val sourceName: VariableName,
    private val type: TypeName,
    private val validationInfo: ValidationInfo
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("${sourceName.name}.validateUnsafe(")
        type.render(this)
        writeln("::asSafe)")
        if (validationInfo.required) {
            writeln(".required()")
        }

    }
}
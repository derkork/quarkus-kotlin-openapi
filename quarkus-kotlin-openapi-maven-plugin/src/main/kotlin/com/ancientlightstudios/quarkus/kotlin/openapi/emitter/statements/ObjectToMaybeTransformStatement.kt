package com.ancientlightstudios.quarkus.kotlin.openapi.emitter.statements

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinStatement
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class ObjectBodyToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: StringExpression, private val type: ClassName, private val required: Boolean
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        write("val ${targetName.render()} = ${sourceName.render()}.asObject(${context.evaluate()}, ${type.render()}::class.java, objectMapper)")
        indent {
            NestedObjectTransformStatement(null, type, required).render(this)
        }
    }
}

class ObjectPropertyToMaybeTransformStatement(
    private val targetName: VariableName, private val sourceName: VariableName,
    private val context: StringExpression, private val type: ClassName, private val required: Boolean
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        writeln("val ${targetName.render()} = ${sourceName.render()}.asMaybe(${context.evaluate()})")
        indent {
            NestedObjectTransformStatement(null, type, required).render(this)
        }
    }
}

class NestedObjectTransformStatement(
    private val sourceName: VariableName?,
    private val type: ClassName,
    private val required: Boolean
) : KotlinStatement {

    override fun render(writer: CodeWriter) = with(writer) {
        sourceName?.let { write(it.render()) }
        write(".validateUnsafe(${type.render()}::asSafe)")
        if (required) {
            writeln(".required()")
        }

    }
}
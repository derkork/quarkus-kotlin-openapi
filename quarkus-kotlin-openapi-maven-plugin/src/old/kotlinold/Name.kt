package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlinold

import com.ancientlightstudios.quarkus.kotlin.openapi.writer.CodeWriter
import java.util.*



interface Expression {
    val expression: String
}


class NestedPathExpression private constructor(private val propertyPath: Expression, private val append: VariableName) :
    Expression {
    override fun toString() = "NestedPath($expression)"
    override val expression: String
        get() = "${propertyPath.expression}.${append.name}"

    companion object {
        fun Expression.nested(name: VariableName) = NestedPathExpression(this, name)
    }
}

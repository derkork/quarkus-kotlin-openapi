package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

class ArrayExpression(private vararg val  expressions: Expression) : Expression {

    override fun evaluate() = expressions.joinToString(", ", prefix = "[", postfix="]") { it.evaluate() }

    companion object {
        fun Array<Expression>.arrayExpression() = ArrayExpression(*this)
        fun Expression.arrayExpression() = ArrayExpression(this)
    }
}
package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.Name

class InvocationExpression(private val name: Expression, vararg var parameter: Expression) : Expression {

    override fun evaluate() = parameter.joinToString(prefix = "${name.evaluate()}(", postfix = ")") { it.evaluate() }

    companion object {

        fun Name.invocationExpression(vararg parameter: Expression) =
            InvocationExpression(this.pathExpression(), *parameter)

        fun Expression.invocationExpression(vararg parameter: Expression) = InvocationExpression(this, *parameter)

    }

}



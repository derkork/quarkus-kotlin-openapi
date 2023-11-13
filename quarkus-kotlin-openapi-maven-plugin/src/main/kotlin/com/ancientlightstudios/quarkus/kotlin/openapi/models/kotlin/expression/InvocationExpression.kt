package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.PathExpression.Companion.pathExpression
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.Name
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.VariableName

class InvocationExpression(private val name: Expression, vararg var parameter: Pair<VariableName?, Expression>) : Expression {

    override fun evaluate() = parameter.joinToString(prefix = "${name.evaluate()}(", postfix = ")") { (name, expression) ->
        if (name != null) {
            "${name.render()} = ${expression.evaluate()}"
        } else {
            expression.evaluate()
        }
    }

    companion object {

        fun Expression.invocationExpression(vararg parameter: Expression)
            = InvocationExpression(this, *parameter.map { null to it }.toTypedArray())

        fun Expression.invocationExpression(vararg parameter: Pair<VariableName?, Expression>)
            =InvocationExpression(this, *parameter)

    }

}



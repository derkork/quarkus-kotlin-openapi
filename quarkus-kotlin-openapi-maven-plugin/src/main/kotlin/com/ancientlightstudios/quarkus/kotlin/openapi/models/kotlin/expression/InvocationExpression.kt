package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.Name

class InvocationExpression(private val name: Name, vararg var parameter: Expression) : Expression {

    override fun evaluate() = parameter.joinToString(prefix = "${name.render()}(", postfix = ")") { it.evaluate() }

    companion object {

        fun Name.invocationExpression(vararg parameter: Expression) = InvocationExpression(this, *parameter)

    }

}



package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName

class ExtendFromClassExpression(private val name: ClassName, private vararg val parameter: Expression) :
    ExtendExpression {

    override fun evaluate() = parameter.joinToString(prefix = "${name.render()}(", postfix = ")") { it.evaluate() }

}
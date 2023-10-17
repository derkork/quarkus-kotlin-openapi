package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

data class IntExpression(val value: String) : Expression {

    override fun evaluate() = value

    companion object {

        fun String.intExpression() = IntExpression(this)

    }

}
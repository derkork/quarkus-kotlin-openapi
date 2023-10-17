package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

data class BooleanExpression(val value: String) : Expression {

    override fun evaluate() = value

    companion object {

        fun String.booleanExpression() = BooleanExpression(this)

    }

}
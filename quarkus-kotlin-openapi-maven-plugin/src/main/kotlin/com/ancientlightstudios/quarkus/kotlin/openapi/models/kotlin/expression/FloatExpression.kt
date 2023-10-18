package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

data class FloatExpression(val value: String) : Expression {

    override fun evaluate() = "${value}F"

    companion object {

        fun String.floatExpression() = FloatExpression(this)

    }

}
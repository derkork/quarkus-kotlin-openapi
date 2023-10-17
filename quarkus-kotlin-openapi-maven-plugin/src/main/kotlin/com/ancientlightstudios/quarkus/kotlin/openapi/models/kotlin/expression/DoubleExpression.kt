package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

data class DoubleExpression(val value: String) : Expression {

    override fun evaluate() = when (value.contains('.')) {
        true -> value
        false -> "${value}.0"
    }

    companion object {

        fun String.doubleExpression() = DoubleExpression(this)

    }

}
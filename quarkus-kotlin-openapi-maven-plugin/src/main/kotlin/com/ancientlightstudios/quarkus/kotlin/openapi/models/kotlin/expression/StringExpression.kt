package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

data class StringExpression(val value: String) : Expression {

    override fun evaluate() = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

    companion object {

        fun String.stringExpression() = StringExpression(this)

    }

}
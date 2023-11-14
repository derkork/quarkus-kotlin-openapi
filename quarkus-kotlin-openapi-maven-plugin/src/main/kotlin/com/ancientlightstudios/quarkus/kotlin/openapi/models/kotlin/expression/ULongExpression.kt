package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

data class ULongExpression(val value: String) : Expression {

    override fun evaluate() = value + "UL"

    companion object {

        fun String.uLongExpression() = ULongExpression(this)

    }
}
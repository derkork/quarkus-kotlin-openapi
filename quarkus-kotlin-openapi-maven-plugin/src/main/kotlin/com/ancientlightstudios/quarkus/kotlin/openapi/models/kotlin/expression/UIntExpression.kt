package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

data class UIntExpression(val value: String) : Expression {

    override fun evaluate() = value + "U"

    companion object {

        fun String.uIntExpression() = UIntExpression(this)

    }
}
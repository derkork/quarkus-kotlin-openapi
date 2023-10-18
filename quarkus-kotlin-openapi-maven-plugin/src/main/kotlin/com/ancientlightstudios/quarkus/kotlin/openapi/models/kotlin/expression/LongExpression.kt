package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

data class LongExpression(val value: String) : Expression {

    override fun evaluate() = "${value}L"

    companion object {

        fun String.longExpression() = LongExpression(this)

    }

}
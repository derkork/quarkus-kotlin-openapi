package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression.StringExpression.Companion.stringExpression

data class StringExpression(val value: String) : Expression {

    override fun evaluate() = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""


    companion object {

        fun String.stringExpression() = StringExpression(this)

    }

}
fun main() {
    val x = "\\+foo".stringExpression()
    val y = x.evaluate()
    println(y)
}
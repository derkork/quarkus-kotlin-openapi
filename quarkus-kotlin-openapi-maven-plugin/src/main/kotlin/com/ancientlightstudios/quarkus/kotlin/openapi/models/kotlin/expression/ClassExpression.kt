package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.expression

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName

@Suppress("DataClassPrivateConstructor")
data class ClassExpression private constructor(val className:ClassName) : Expression {

    override fun evaluate() = "${className.render()}::class"


    companion object {
        fun ClassName.classExpression() = ClassExpression(this)

    }

}
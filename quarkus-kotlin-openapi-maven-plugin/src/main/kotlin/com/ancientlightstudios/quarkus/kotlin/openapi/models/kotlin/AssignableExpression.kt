package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class AssignableExpression(
    private val receiver: KotlinExpression?, private val typeReference: KotlinTypeReference
) : KotlinExpression {

    override fun ImportCollector.registerImports() {
        receiver?.let { registerFrom(it) }
        register(typeReference)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        receiver?.let {
            it.render(this)
            write(" ")
        }

        write("is ${typeReference.render()}")
    }

    companion object {

        fun assignable(typeReference: KotlinTypeReference): KotlinExpression =
            AssignableExpression(null, typeReference)

        fun KotlinExpression.assignable(typeReference: KotlinTypeReference): KotlinExpression =
            AssignableExpression(this, typeReference)

    }

}



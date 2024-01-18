package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class NullCheckExpression(private val receiver: KotlinExpression) : KotlinExpression {

    override fun ImportCollector.registerImports() {
        registerFrom(receiver)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        receiver.render(this)
        write("?")
    }

    companion object {

        fun KotlinExpression.nullCheck() = NullCheckExpression(this)

    }

}
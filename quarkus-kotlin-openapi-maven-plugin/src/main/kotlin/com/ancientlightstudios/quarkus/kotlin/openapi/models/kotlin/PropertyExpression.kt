package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter


class PropertyExpression(private val receiver: KotlinExpression, private val name: VariableName) : KotlinExpression {

    override fun ImportCollector.registerImports() {
        registerFrom(receiver)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        receiver.render(this)
        write(".${name.value}")
    }

    companion object {

        fun KotlinExpression.property(name: VariableName) = PropertyExpression(this, name)

    }

}
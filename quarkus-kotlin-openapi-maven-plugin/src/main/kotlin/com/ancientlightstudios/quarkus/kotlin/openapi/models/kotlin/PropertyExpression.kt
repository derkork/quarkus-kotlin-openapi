package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter


class PropertyExpression(private val receiver: KotlinExpression, private val name: KotlinExpression) :
    KotlinExpression {

    override fun ImportCollector.registerImports() {
        registerFrom(receiver)
        registerFrom(name)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        receiver.render(this)
        write(".")
        name.render(this)
    }

    companion object {

        fun KotlinExpression.property(name: VariableName) = PropertyExpression(this, name)

        fun KotlinExpression.property(name: ClassName) = PropertyExpression(this, name.companionObject())

    }

}
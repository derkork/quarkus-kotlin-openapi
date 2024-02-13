package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class AssignableExpression(
    private val receiver: KotlinExpression?, private val className: ClassName
) : KotlinExpression {

    override fun ImportCollector.registerImports() {
        receiver?.let { registerFrom(it) }
        register(className)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        receiver?.let {
            it.render(this)
            write(" ")
        }

        write(" is ${className.value}")
    }

    companion object {

        fun assignable(className: ClassName): KotlinExpression =
            AssignableExpression(null, className)

        fun KotlinExpression.assignable(className: ClassName): KotlinExpression =
            AssignableExpression(this, className)

    }

}



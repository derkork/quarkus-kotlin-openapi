package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

class CompanionObjectExpression(private val className: ClassName) : KotlinExpression {

    override fun ImportCollector.registerImports() {
        register(className)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write(className.value)
    }

    companion object {

        fun ClassName.companionObject() = CompanionObjectExpression(this)

    }

}

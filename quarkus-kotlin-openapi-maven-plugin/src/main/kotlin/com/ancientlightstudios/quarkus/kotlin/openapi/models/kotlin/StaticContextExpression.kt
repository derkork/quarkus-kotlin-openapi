package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.KotlinTypeName.Companion.asTypeName
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ComponentName


class StaticContextExpression(private val receiver: KotlinTypeName) : KotlinExpression {

    override fun ImportCollector.registerImports() {
        register(receiver)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write(receiver.name)
    }

    companion object {

        fun ComponentName.staticContext() = asTypeName().staticContext()

        fun KotlinTypeName.staticContext() = StaticContextExpression(this)

    }

}
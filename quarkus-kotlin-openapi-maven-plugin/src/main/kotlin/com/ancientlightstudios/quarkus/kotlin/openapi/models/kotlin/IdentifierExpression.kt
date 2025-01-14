package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

// TODO: check if handling packages this way is a good idea, or if we should change the signature where a identifier is used
class IdentifierExpression(val value: String, val packageName: String = "") : KotlinExpression {

    override fun ImportCollector.registerImports() {
        register(value, packageName)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write(value)

    }

    companion object {

        fun KotlinTypeName.identifier() = name.identifier(packageName)

        fun String.identifier(packageName: String = "") = IdentifierExpression(this, packageName)

    }

}
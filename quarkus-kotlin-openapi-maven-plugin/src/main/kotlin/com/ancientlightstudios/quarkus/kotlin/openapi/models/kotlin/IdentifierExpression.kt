package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.models.solution.ComponentName

class IdentifierExpression(val value: String, val packageName: String = "") : KotlinExpression {

    override fun ImportCollector.registerImports() {
        register(value, packageName)
    }

    override fun render(writer: CodeWriter) = with(writer) {
        write(value)

    }

    companion object {

        fun ComponentName.identifier() = name.identifier(packageName)

        fun KotlinTypeName.identifier() = name.identifier(packageName)

        fun String.identifier(packageName: String = "") = IdentifierExpression(this, packageName)

        fun ComponentName.companionMethod(methodName: String) =
            IdentifierExpression(methodName, "$packageName.$name.Companion")

    }

}
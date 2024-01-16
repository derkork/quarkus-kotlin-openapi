package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter

@Suppress("DataClassPrivateConstructor")
data class VariableName private constructor(val value: String) : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) {
        writer.write(value)
    }

    companion object {

        fun String.rawVariableName() = VariableName(this)

        fun String.variableName(prefix: String = "", postfix: String = "") =
            VariableName("$prefix $this $postfix".toKotlinIdentifier())

    }

}
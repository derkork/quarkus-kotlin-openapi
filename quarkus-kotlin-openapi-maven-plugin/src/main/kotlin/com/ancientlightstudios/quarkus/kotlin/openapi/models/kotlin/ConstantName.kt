package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.emitter.CodeWriter
import java.util.*

@Suppress("DataClassPrivateConstructor")
data class ConstantName private constructor(val value: String) : KotlinExpression {

    override fun ImportCollector.registerImports() {}

    override fun render(writer: CodeWriter) {
        writer.write(value)
    }

    companion object {

        fun String.rawConstantName() = ConstantName(this)

        fun String.constantName(prefix: String = "", postfix: String = "") =
            ConstantName("$prefix $this $postfix".toKotlinIdentifier()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() })

    }

}
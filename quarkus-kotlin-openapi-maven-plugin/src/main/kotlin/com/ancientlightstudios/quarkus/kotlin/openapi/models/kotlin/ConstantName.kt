package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import java.util.*

@Suppress("DataClassPrivateConstructor")
data class ConstantName private constructor(val value: String) {

    // TODO: optional import-source for raw and normal methods?

    companion object {

        fun String.rawConstantName() = ConstantName(this)

        fun String.constantName(prefix: String = "", postfix: String = "") =
            ConstantName("$prefix $this $postfix".toKotlinIdentifier()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() })

    }

}
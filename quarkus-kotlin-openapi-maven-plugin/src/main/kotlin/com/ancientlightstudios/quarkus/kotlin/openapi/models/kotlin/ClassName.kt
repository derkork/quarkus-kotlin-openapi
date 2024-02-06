package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import java.util.*

@Suppress("DataClassPrivateConstructor")
data class ClassName private constructor(val packageName: String, val value: String, val provided: Boolean) {

    fun extend(prefix: String = "", postfix: String = "", packageName: String = this.packageName) =
        value.className(packageName, prefix, postfix)

    companion object {

        fun String.rawClassName(packageName: String, provided: Boolean = false) = ClassName(packageName, this, provided)

        fun String.className(packageName: String, prefix: String = "", postfix: String = "") =
            "$prefix $this $postfix".toKotlinIdentifier()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
                .let { ClassName(packageName, it, false) }

    }

}

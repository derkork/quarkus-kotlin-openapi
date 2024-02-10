package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import java.util.*

@Suppress("DataClassPrivateConstructor")
data class ClassName private constructor(val value: String, val packageName: String, val provided: Boolean) {

    fun extend(prefix: String = "", postfix: String = "", packageName: String = this.packageName) =
        value.className(packageName, prefix, postfix)

    val constructorName = value.rawMethodName(packageName, provided)

    companion object {

        fun String.rawClassName(packageName: String, provided: Boolean = false) = ClassName(this, packageName, provided)

        fun String.className(packageName: String, prefix: String = "", postfix: String = "") =
            "$prefix $this $postfix".toKotlinIdentifier()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
                .let { ClassName(it, packageName, false) }

    }

}

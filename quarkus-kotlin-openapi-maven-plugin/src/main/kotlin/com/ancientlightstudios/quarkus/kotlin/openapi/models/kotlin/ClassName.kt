package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.ClassName.Companion.rawClassName
import java.util.*

@Suppress("DataClassPrivateConstructor")
data class ClassName private constructor(val packageName: String, val name: String, val provided: Boolean) {

    fun extend(prefix: String = "", postfix: String = "", packageName: String = this.packageName) =
        name.className(packageName, prefix, postfix)

    companion object {

        fun String.className(packageName: String, prefix: String = "", postfix: String = "") =
            "$prefix $this $postfix".toKotlinIdentifier()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
                .let { ClassName(packageName, it, false) }

        fun String.rawClassName(packageName: String, provided: Boolean) = ClassName(packageName, this, provided)

    }

}

object Kotlin {

    val StringClass = "String".rawClassName("kotlin", true)

}
package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin.MethodName.Companion.rawMethodName
import java.util.*

@Suppress("DataClassPrivateConstructor")
data class ClassName private constructor(val value: String, val packageName: String, val provided: Boolean) {

    fun extend(prefix: String = "", postfix: String = "", packageName: String = this.packageName) =
        value.className(packageName, prefix, postfix)

    val constructorName = value.rawMethodName(packageName, provided)

    fun nested(value: String) = "${this.value}.${value.asClassName()}".rawClassName(packageName)

    fun rawNested(value: String) = "${this.value}.$value".rawClassName(packageName)

    companion object {

        fun String.rawClassName(packageName: String, provided: Boolean = false) = ClassName(this, packageName, provided)

        fun String.className(packageName: String, prefix: String = "", postfix: String = "") =
            ClassName("$prefix $this $postfix".asClassName(), packageName, false)

        private fun String.asClassName() = toKotlinIdentifier()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }

    }

}

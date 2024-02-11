package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

@Suppress("DataClassPrivateConstructor")
data class MethodName private constructor(val value: String, val packageName: String, val provided: Boolean) {

    fun extend(prefix: String = "", postfix: String = "") = value.methodName(packageName, prefix, postfix)

    companion object {

        fun String.rawMethodName(packageName: String = "", provided: Boolean = false) =
            MethodName(this, packageName, provided)

        fun String.methodName(packageName: String = "", prefix: String = "", postfix: String = "") =
            MethodName("$prefix $this $postfix".toKotlinIdentifier(), packageName, false)

        fun ClassName.companionMethod(name: String) =
            MethodName(name.toKotlinIdentifier(), "$packageName.$value.Companion", false)

    }

}
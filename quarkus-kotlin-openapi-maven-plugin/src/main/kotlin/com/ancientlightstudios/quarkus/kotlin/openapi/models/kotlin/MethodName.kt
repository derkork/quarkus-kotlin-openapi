package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

@Suppress("DataClassPrivateConstructor")
data class MethodName private constructor(val value: String) {

    // TODO: optional import-source for raw and normal methods?

    fun extend(prefix: String = "", postfix: String = "") = value.methodName(prefix, postfix)

    companion object {

        fun String.rawMethodName() = MethodName(this)

        fun String.methodName(prefix: String = "", postfix: String = "") =
            MethodName("$prefix $this $postfix".toKotlinIdentifier())

    }

}
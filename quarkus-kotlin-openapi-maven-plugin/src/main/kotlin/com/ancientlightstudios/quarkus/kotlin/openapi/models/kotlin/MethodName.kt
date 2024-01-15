package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

@Suppress("DataClassPrivateConstructor")
data class MethodName private constructor(val name: String) {

    // TODO: optional import-source for raw and normal methods

    fun extend(prefix: String = "", postfix: String = "") = name.methodName(prefix, postfix)

    companion object {

        fun String.methodName(prefix: String = "", postfix: String = "") =
            MethodName("$prefix $this $postfix".toKotlinIdentifier())

        fun String.rawMethodName() = MethodName(this)

    }

}
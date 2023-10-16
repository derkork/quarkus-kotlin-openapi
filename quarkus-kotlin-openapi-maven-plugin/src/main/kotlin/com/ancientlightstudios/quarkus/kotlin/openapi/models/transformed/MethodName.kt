package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed

import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.ClassName.Companion.className
import com.ancientlightstudios.quarkus.kotlin.openapi.utils.toKotlinIdentifier

@Suppress("DataClassPrivateConstructor")
data class MethodName private constructor(private val name: String) {

    fun extend(prefix: String = "", postfix: String = "") = "${prefix}_${name}_$postfix".methodName()

    fun render() = name

    companion object {

        fun String.methodName() = MethodName(toKotlinIdentifier())

    }

}
package com.ancientlightstudios.quarkus.kotlin.openapi.models.kotlin

import com.ancientlightstudios.quarkus.kotlin.openapi.strafbank.CodeWriter
import com.ancientlightstudios.quarkus.kotlin.openapi.strafbank.toKotlinClassName
import com.ancientlightstudios.quarkus.kotlin.openapi.strafbank.toKotlinIdentifier

sealed class Name(val name: String) {

    class ClassName(name: String) : Name(name.toKotlinClassName())
    class VariableName(name: String) : Name(name.toKotlinIdentifier())
    class MethodName(name: String) : Name(name.toKotlinIdentifier())

}
package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

enum class SchemaTypes(val value: kotlin.String) {
    String("string"),
    Number("number"),
    Integer("integer"),
    Boolean("boolean"),
    Object("object"),
    Array("array");

    companion object {

        fun fromString(value: kotlin.String) = values().firstOrNull { it.value == value }
            ?: SpecIssue("Unsupported schema type '$value'")

    }

}
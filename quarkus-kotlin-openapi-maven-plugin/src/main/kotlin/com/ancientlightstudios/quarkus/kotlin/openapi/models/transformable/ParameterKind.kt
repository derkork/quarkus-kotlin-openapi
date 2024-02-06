package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

enum class ParameterKind(val value: String) {
    Path("path"),
    Query("query"),
    Header("header"),
    Cookie("cookie");

    companion object {

        fun fromString(value: String) = values().firstOrNull { it.value == value.lowercase() }
            ?: SpecIssue("Unknown parameter kind '$value'")

    }

}
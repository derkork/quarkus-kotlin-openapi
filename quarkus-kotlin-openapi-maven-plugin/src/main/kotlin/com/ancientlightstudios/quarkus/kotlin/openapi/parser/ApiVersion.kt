package com.ancientlightstudios.quarkus.kotlin.openapi.parser

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

enum class ApiVersion(private val value: String) {
    V3_0("3.0."),
    V3_1("3.1.");

    companion object {

        fun fromString(value: String) = values().firstOrNull { value.startsWith(it.value) }
            ?: SpecIssue("Unsupported OpenAPI version '$value'")

    }

}
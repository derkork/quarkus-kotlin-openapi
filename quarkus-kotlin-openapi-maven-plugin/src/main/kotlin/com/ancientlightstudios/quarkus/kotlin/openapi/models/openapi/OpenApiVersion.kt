package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

enum class OpenApiVersion(private val value: String) {
    V3_0("3.0."),
    V3_1("3.1.");

    companion object {

        fun fromString(value: String) = OpenApiVersion.values().firstOrNull { value.startsWith(it.value) }
            ?: throw IllegalArgumentException("Unsupported OpenAPI version $value")

    }

}
package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

enum class ContentType(val value: String) {
    ApplicationJson("application/json");
    // TODO add text/plain and other stuff

    companion object {

        fun fromString(value: String) = values().firstOrNull { it.value == value.lowercase() }
            ?: SpecIssue("Unsupported content type '$value'")

    }

}
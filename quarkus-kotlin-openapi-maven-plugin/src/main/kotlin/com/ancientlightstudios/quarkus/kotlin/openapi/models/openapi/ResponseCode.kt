package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

sealed interface ResponseCode {

    object Default : ResponseCode

    @JvmInline
    value class HttpStatusCode(val value: Int) : ResponseCode

    companion object {

        fun fromString(value: String) = when (value) {
            "default" -> Default
            else -> value.toIntOrNull()?.let { HttpStatusCode(it) }
                ?: SpecIssue("invalid response code '$value'.")

        }

    }

}
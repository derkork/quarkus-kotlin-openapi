package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

sealed interface ResponseCode {

    object Default : ResponseCode {

        override fun toString() = "Default"

    }

    @JvmInline
    value class HttpStatusCode(val value: Int) : ResponseCode {

        override fun toString() = value.toString()

    }

    companion object {

        fun fromString(value: String) = when (value) {
            "default" -> Default
            else -> value.toIntOrNull()?.let { HttpStatusCode(it) }
                ?: SpecIssue("invalid response code '$value'.")

        }

    }

}
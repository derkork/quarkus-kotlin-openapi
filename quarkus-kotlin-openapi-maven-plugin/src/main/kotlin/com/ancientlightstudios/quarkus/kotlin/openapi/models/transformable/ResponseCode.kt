package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue
import jakarta.ws.rs.core.Response

sealed interface ResponseCode {

    object Default : ResponseCode {

        override fun toString() = "Default"

    }

    @JvmInline
    value class HttpStatusCode(val value: Int) : ResponseCode {

        override fun toString() = value.toString()

        fun statusCodeReason() = Response.Status.fromStatusCode(value)?.reasonPhrase ?: "status${this}"

        fun statusCodeName() = Response.Status.fromStatusCode(value)?.name ?: "status${this}"

    }

    companion object {

        fun fromString(value: String) = when (value) {
            "default" -> Default
            else -> value.toIntOrNull()?.let { HttpStatusCode(it) }
                ?: SpecIssue("invalid response code '$value'.")

        }

    }

}
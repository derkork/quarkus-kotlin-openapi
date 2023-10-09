package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

sealed interface ResponseCode {

    object Default : ResponseCode

    @JvmInline
    value class HttpStatusCode(val value: Int) : ResponseCode

    companion object {

        fun fromString(value: String) = when (value) {
            "default" -> Default
            else -> HttpStatusCode(value.toInt())
        }

    }

}
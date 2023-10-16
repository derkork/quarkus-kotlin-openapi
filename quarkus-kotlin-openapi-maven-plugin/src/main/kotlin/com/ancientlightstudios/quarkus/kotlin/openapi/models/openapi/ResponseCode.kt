package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

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
            else -> HttpStatusCode(value.toInt())
        }

    }

}
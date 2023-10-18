package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

enum class RequestMethod(private val value: String) {
    Get("get"),
    Put("put"),
    Post("post"),
    Delete("delete"),
    Options("options"),
    Head("head"),
    Patch("patch"),
    Trace("trace");

    companion object {

        fun fromString(value: String) = RequestMethod.values().firstOrNull { it.value == value.lowercase() }
            ?: throw IllegalArgumentException("Unknown request method $value")

    }

}

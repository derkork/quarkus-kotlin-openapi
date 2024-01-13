package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformable

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

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

        fun fromString(value: String) = values().firstOrNull { it.value == value.lowercase() }
            ?: SpecIssue("Unknown request method '$value'")

    }

}
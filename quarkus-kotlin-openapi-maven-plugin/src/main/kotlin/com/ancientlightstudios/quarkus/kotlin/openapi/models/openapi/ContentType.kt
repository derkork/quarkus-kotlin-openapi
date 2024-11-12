package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi

import com.ancientlightstudios.quarkus.kotlin.openapi.utils.SpecIssue

enum class ContentType(val value: String) {
    ApplicationJson("application/json"),
    TextPlain("text/plain"),
//    MultipartFormData("multipart/form-data"),
    ApplicationFormUrlencoded("application/x-www-form-urlencoded"),
    ApplicationOctetStream("application/octet-stream");

    companion object {

        fun fromString(value: String) = values().firstOrNull { it.value == value.lowercase() }
            ?: SpecIssue("Unsupported content type '$value'")

    }

}
package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation

sealed interface Validation {

    val customConstraints: List<String>

}
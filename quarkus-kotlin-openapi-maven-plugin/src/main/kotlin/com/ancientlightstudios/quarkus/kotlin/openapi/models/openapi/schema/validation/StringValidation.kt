package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation

data class StringValidation(val minLength: Int?, val maxLength: Int?, val pattern: String?) : Validation
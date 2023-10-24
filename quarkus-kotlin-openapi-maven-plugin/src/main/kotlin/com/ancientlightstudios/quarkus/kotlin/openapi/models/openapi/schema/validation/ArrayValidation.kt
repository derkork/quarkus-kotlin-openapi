package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation

data class ArrayValidation(
    val minSize: Int?,
    val maxSize: Int?,
    override val customConstraints: List<String>
) : Validation {

    val hasArrayValidationRules = minSize != null || maxSize != null

}
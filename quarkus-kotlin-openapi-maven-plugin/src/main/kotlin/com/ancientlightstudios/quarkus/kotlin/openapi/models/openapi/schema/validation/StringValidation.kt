package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation

data class StringValidation(
    val minLength: Int?,
    val maxLength: Int?,
    val pattern: String?,
    override val customConstraints: List<String>
) : Validation {

    val hasStringValidationRules = minLength != null || maxLength != null || pattern != null

}
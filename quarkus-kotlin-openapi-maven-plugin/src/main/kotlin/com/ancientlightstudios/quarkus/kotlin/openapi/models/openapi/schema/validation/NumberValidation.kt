package com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation

data class NumberValidation(
    val minimum: ComparableNumber?,
    val maximum: ComparableNumber?,
) : Validation {

    val hasNumberValidationRules = minimum != null || maximum != null

}

data class ComparableNumber(val value: String, val exclusive: Boolean)
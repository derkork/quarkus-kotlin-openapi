package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation

sealed interface TypeDefinition {

    fun useAs(valueRequired: Boolean): TypeDefinitionUsage

    val validations: List<Validation>

}
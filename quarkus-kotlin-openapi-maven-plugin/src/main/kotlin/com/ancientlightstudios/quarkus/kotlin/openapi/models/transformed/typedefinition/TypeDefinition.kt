package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

import com.ancientlightstudios.quarkus.kotlin.openapi.models.openapi.schema.validation.Validation
import com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.name.ClassName

sealed interface TypeDefinition {

    val name:ClassName

    fun useAs(valueRequired: Boolean): TypeDefinitionUsage

    val validations: List<Validation>

}
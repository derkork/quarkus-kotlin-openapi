package com.ancientlightstudios.quarkus.kotlin.openapi.models.transformed.typedefinition

sealed interface TypeDefinition {

    fun useAs(valueRequired: Boolean): TypeDefinitionUsage

}